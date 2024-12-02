package org.example.domain.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import org.example.domain.models.Caminho
import org.example.domain.models.VerticeInfo
import org.example.utils.Constants
import org.example.utils.messageToVerticeInfo
import java.util.*

class Service {
    private val verticesVisitados: MutableList<VerticeInfo> = mutableListOf()
    private val verticesInfoSaidas: MutableList<VerticeInfo> = mutableListOf()
    private var primeiraVez = true
    private var melhorCaminho: List<Int> = emptyList()
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    suspend fun iniciaWS(url: String): List<Int> {
        client.webSocket(host = Constants.IP, port = Constants.PORT.toInt(), path = url) {
            tremaux(this, 0)

            if (melhorCaminho.isNotEmpty()) {
                println("Melhor caminho encontrado: ${melhorCaminho.joinToString(" -> ")}")
            } else {
                println("Não foi encontrado um caminho.")
            }

            close()
            return@webSocket
        }
        return melhorCaminho
    }

    private suspend fun getVerticeInfo(sessao: DefaultClientWebSocketSession, destination: Int): VerticeInfo? {
        if (!primeiraVez) {
            println("ir: $destination")
            sessao.send("ir:$destination")
        }

        return try {
            for (message in sessao.incoming) {
                when (message) {
                    is Frame.Text -> {
                        val text = message.readText()
                        println(text)
                        return messageToVerticeInfo(text)
                    }

                    is Frame.Close -> {
                        println("Conexão fechada")
                        break
                    }

                    else -> {
                    }
                }
            }
            null
        } catch (e: Exception) {
            println("Erro ao receber mensagens: ${e.message}")
            null
        }
    }

    private suspend fun tremaux(sessao: DefaultClientWebSocketSession, startVertex: Int) {
        val visited = mutableSetOf<Int>()
        val stack: Stack<Int> = Stack()
        var last = startVertex
        var casos = 0

        while (melhorCaminho.isEmpty()) {
            casos++
            println("Caso $casos")
            stack.push(last)
            visited.clear()
            while (stack.isNotEmpty()) {
                println("stack-> $stack")
                val currentVertex = stack.peek()

                if (!visited.contains(currentVertex)) {
                    visited.add(currentVertex)
                    println("Visitando vértice $currentVertex")
                }

                getVerticeInfo(sessao, currentVertex)?.let { verticeInfo ->
                    if (verticeInfo.adjacentes.isNotEmpty()) {
                        println("LAST: ${verticeInfo.adjacentes.map { it.vertice }}")
                        println("LAST:${verticeInfo.adjacentes[0].vertice}")
                        last = verticeInfo.adjacentes[0].vertice
                    }

                    if (!verticesVisitados.contains(verticeInfo)) {
                        verticesVisitados.add(verticeInfo)
                        if (verticeInfo.tipo == 2) {
                            verticesInfoSaidas.add(verticeInfo)
                            println("Procurando o melhor caminho...")
                            melhorCaminho = estrela(verticesVisitados, verticesInfoSaidas)
                            if (melhorCaminho.isNotEmpty()) {
                                return
                            }
                        }
                    }
                    primeiraVez = false

                    verticeInfo.adjacentes.firstOrNull { adj ->
                        !visited.contains(adj.vertice)
                    }?.let { proxVertice ->
                        println("Adicionando vértice ${proxVertice.vertice} à pilha")
                        stack.push(proxVertice.vertice)
                    } ?: run {
                        println("Beco sem saída em $currentVertex, retornando...")
                        stack.pop()
                    }
                } ?: run {
                    println("Vértice inválido, retornando...")
                    stack.pop()
                }
            }

            println("Saídas: ${verticesInfoSaidas.map { v -> v.verticeAtual }}")
            println("Visitados: ${verticesVisitados.map { v -> v.verticeAtual }}")
        }
    }

    private fun estrela(verticesVisitados: List<VerticeInfo>, verticesInfoSaidas: List<VerticeInfo>): List<Int> {
        try {
            if (verticesVisitados.isEmpty() || verticesInfoSaidas.isEmpty()) {
                println("Os vértices visitados ou de saída estão vazios.")
                return emptyList()
            }

            val inicio = verticesVisitados.firstOrNull() ?: return emptyList()

            val saidas = verticesInfoSaidas.map { it.verticeAtual }.toSet()

            val openList = PriorityQueue<Caminho> { a, b ->
                (a.custoAcumulado + heuristica(
                    verticesVisitados.find { it.verticeAtual == a.vertice },
                    verticesInfoSaidas.firstOrNull()
                )) -
                        (b.custoAcumulado + heuristica(
                            verticesVisitados.find { it.verticeAtual == b.vertice },
                            verticesInfoSaidas.firstOrNull()
                        ))
            }

            val gCosts = mutableMapOf<Int, Int>().apply {
                put(inicio.verticeAtual, 0)
            }
            val cameFrom = mutableMapOf<Int, Int>()

            openList.add(Caminho(inicio.verticeAtual, 0, listOf(inicio.verticeAtual)))

            while (openList.isNotEmpty()) {
                val current = openList.poll()

                if (current.vertice in saidas) {
                    val caminhoFinal = mutableListOf<Int>()
                    var verticeAtual = current.vertice
                    while (cameFrom.containsKey(verticeAtual)) {
                        caminhoFinal.add(0, verticeAtual)
                        verticeAtual = cameFrom[verticeAtual]!!
                    }
                    caminhoFinal.add(0, inicio.verticeAtual)
                    return caminhoFinal
                }

                val verticeAtualInfo = verticesVisitados.find { it.verticeAtual == current.vertice }
                verticeAtualInfo?.adjacentes?.forEach { adj ->
                    val novoCusto = current.custoAcumulado + adj.peso
                    if (novoCusto < gCosts.getOrDefault(adj.vertice, Int.MAX_VALUE)) {
                        gCosts[adj.vertice] = novoCusto
                        cameFrom[adj.vertice] = current.vertice
                        openList.add(Caminho(adj.vertice, novoCusto, current.caminho + adj.vertice))
                    }
                }
            }

            println("Caminho não encontrado.")
        } catch (e: Exception) {
            println("Erro ao processar o grafo: ${e.message}")
        }
        return emptyList()
    }

    private fun heuristica(v: VerticeInfo?, objetivo: VerticeInfo?): Int {
        if (v == null || objetivo == null) return Int.MAX_VALUE
        return v.adjacentes.find { it.vertice == objetivo.verticeAtual }?.peso ?: Int.MAX_VALUE
    }

    fun isFinalizar(): Boolean {
        println(
            "Deseja sair?\n" +
                    "1 - sair\n" +
                    "2 - continuar"
        )
        val sair = readlnOrNull()
        if ((sair ?: "1") == "1") {
            println("Finalizado")
            return true
        }
        return false
    }

}