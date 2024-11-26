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
    private var melhorCaminho:List<Int> = emptyList()
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    suspend fun iniciaWS(url: String): List<Int> {
        client.webSocket(host = Constants.IP, port = Constants.PORT.toInt(), path = url) {
            tremaux(this, 0)
            println("Saídas: ${verticesInfoSaidas.map { v -> v.verticeAtual }}")
            println("Visitados: ${verticesVisitados.map { v -> v.verticeAtual }}")
            println("Procurando o melhor caminho...")
            melhorCaminho = estrela(verticesVisitados, verticesInfoSaidas)

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
//        val returned = mutableSetOf<Int>()
        val stack: Stack<Int> = Stack()
        stack.push(startVertex)

        while (stack.isNotEmpty()) {
            println("stack-> $stack")
            val currentVertex = stack.peek()

            if (!visited.contains(currentVertex)) {
                visited.add(currentVertex)
                println("Visitando vértice $currentVertex")
            }

            getVerticeInfo(sessao, currentVertex)?.let { verticeInfo ->

                if (!verticesVisitados.contains(verticeInfo)) {
                    verticesVisitados.add(verticeInfo)
                    if (verticeInfo.tipo == 2) {
                        verticesInfoSaidas.add(verticeInfo)
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
//                    returned.add(currentVertex)
                    stack.pop()
                }

//                var foundNext = false
//
//                for (adjacente in verticeInfo.adjacentes) {
//                    if (!visited.contains(adjacente.vertice)) {
//                        println("Adicionando vértice ${adjacente.vertice} à pilha")
//                        stack.push(adjacente.vertice)
//                        foundNext = true
//                        break
//                    }
//                }
//
//                if (!foundNext) {
//                    println("Beco sem saída em $currentVertex, retornando...")
//                    returned.add(currentVertex)
//                    stack.pop()
//                }
            } ?: run {
                println("Vértice inválido, retornando...")
//                returned.add(currentVertex)
                stack.pop()
            }
        }
    }

    // Função principal do algoritmo A* para encontrar o melhor caminho
    private fun estrela(verticesVisitados: List<VerticeInfo>, verticesInfoSaidas: List<VerticeInfo>): List<Int> {
        // Inicia com o primeiro vértice da lista de vértices visitados (vértice inicial)
        val inicio = verticesVisitados.first()

        // Cria um conjunto de vértices de saída (objetivos) a partir da lista de vértices de saída fornecida
        val saidas = verticesInfoSaidas.map { it.verticeAtual }.toSet()

        // Fila de prioridade (openList) para o algoritmo A*, baseada na soma dos custos acumulados (G) e da heurística (H)
        val openList = PriorityQueue<Caminho> { a, b ->
            // A comparação entre dois caminhos usa a fórmula F = G + H
            // Onde G é o custo acumulado até o vértice atual e H é a heurística (estimativa do custo para o objetivo)
            // A heurística aqui é a distância entre os vértices, podendo ser aprimorada conforme necessário
            (a.custoAcumulado + heuristica(
                verticesVisitados.find { it.verticeAtual == a.vertice }!!,
                verticesInfoSaidas.first()
            )) -
                    (b.custoAcumulado + heuristica(
                        verticesVisitados.find { it.verticeAtual == b.vertice }!!,
                        verticesInfoSaidas.first()
                    ))
        }

        // Mapa de custos acumulados para cada vértice (G)
        val gCosts = mutableMapOf<Int, Int>().apply {
            put(
                inicio.verticeAtual,
                0
            )
        } // Inicializa o custo do vértice inicial como 0

        // Mapa para armazenar o caminho (cameFrom) - quem foi o vértice anterior de cada vértice
        val cameFrom = mutableMapOf<Int, Int>()

        // Adiciona o vértice inicial na openList (fila de prioridade)
        openList.add(Caminho(inicio.verticeAtual, 0, listOf(inicio.verticeAtual)))

        // Enquanto a openList não estiver vazia, expande os caminhos possíveis
        while (openList.isNotEmpty()) {
            // Pega o vértice com o menor custo acumulado (menor F = G + H)
            val current = openList.poll()

            // Se o vértice atual é um dos vértices de saída, reconstruímos o caminho
            if (current.vertice in saidas) {
                // Inicia a lista do caminho final
                val caminhoFinal = mutableListOf<Int>()
                var verticeAtual = current.vertice

                // Reconstrói o caminho voltando pelos vértices anteriores (cameFrom)
                while (cameFrom.containsKey(verticeAtual)) {
                    // Adiciona o vértice atual no início da lista (para obter o caminho completo)
                    caminhoFinal.add(0, verticeAtual)
                    // Vai para o vértice anterior
                    verticeAtual = cameFrom[verticeAtual]!!
                }

                // Adiciona o vértice inicial no início do caminho
                caminhoFinal.add(0, inicio.verticeAtual)

                // Retorna o caminho completo encontrado
                return caminhoFinal
            }

            // Expande os adjacentes do vértice atual
            verticesVisitados.find { it.verticeAtual == current.vertice }?.adjacentes?.forEach { adj ->
                // Calcula o novo custo acumulado para chegar ao vértice adjacente
                val novoCusto = current.custoAcumulado + adj.peso

                // Se o novo custo for melhor (menor) do que o custo anterior para esse vértice
                if (novoCusto < gCosts.getOrDefault(adj.vertice, Int.MAX_VALUE)) {
                    // Atualiza o custo acumulado para esse vértice
                    gCosts[adj.vertice] = novoCusto
                    // Armazena o vértice anterior no caminho
                    cameFrom[adj.vertice] = current.vertice
                    // Adiciona o novo caminho na openList
                    openList.add(Caminho(adj.vertice, novoCusto, current.caminho + adj.vertice))
                }
            }
        }

        // Se não encontrar caminho até os vértices de saída, retorna uma lista vazia
        return emptyList()
    }

    // Função para calcular a heurística (distância do vértice atual para o objetivo)
    // Exemplo simples de heurística: a distância entre o vértice atual e o objetivo
    private fun heuristica(v: VerticeInfo, objetivo: VerticeInfo): Int {
        // A heurística é simplesmente a distância entre o vértice atual e o objetivo
        // Aqui, estamos considerando o peso das arestas como a distância entre os vértices
        return v.adjacentes.find { it.vertice == objetivo.verticeAtual }?.peso ?: Int.MAX_VALUE
    }

}