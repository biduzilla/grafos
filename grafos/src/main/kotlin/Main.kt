package org.example

import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.example.domain.models.GenerateWs
import org.example.domain.models.Resposta
import org.example.domain.service.Service
import org.example.domain.use_case.Cases
import org.example.utils.Resource

fun main(): Unit = runBlocking {
    val service = Service()
    var idGrupo = ""
    var url: String
    val cases = Cases()
    var isSaida = false
    var saidas: List<Int> = emptyList()
    var idLabirinto: Int = 0

    println("Deseja criar um novo grupo ou já possui um ID de grupo?\n1 - novo\n2 - existente")
    val escolha = readlnOrNull()

    if (escolha?.trim().equals("1", ignoreCase = true)) {
        println("Digite o nome do grupo")
        val nome = readlnOrNull()
        println("Criando um novo grupo...")
        cases.postGrupo(nome ?: "teste").collect { result ->
            when (result) {
                is Resource.Error -> {
                    println(result.message)
                }

                is Resource.Loading -> {
                    println("Enviando...")
                }

                is Resource.Success -> {
                    result.data?.let {
                        idGrupo = it.grupoId
                    }
                }
            }
        }
    } else if (escolha?.trim().equals("2", ignoreCase = true)) {
        println("Digite o ID do grupo:")
        idGrupo = readlnOrNull() ?: ""
    } else {
        println("Opção inválida. Saindo...")
        return@runBlocking
    }

    while (!isSaida) {
        println("Digite o id do labirinto")
        readlnOrNull()?.let { lab ->
            idLabirinto = lab.toInt()
        }

        println("Gerando URL WebSocket para o grupo ID: $idGrupo")
        cases.generateWs(GenerateWs(idGrupo, idLabirinto.toString())).collect { result ->
            when (result) {
                is Resource.Error -> {
                    println(result.message)
                }

                is Resource.Loading -> {
                    println("Enviando...")
                }

                is Resource.Success -> {
                    result.data?.let {
                        url = it.websocketUrl
                        url = url.substring(url.indexOf("/ws/") + 1)
                        println("WebSocket URL: ${it.websocketUrl}")
                        saidas = service.iniciaWS(url)
                    }
                }
            }
        }

        cases.enviarResposta(
            Resposta(
                labirinto =idLabirinto,
                grupo = idGrupo,
                vertices = saidas
            )
        ).collect { result ->
            when (result) {
                is Resource.Error -> {
                    println(result.message)
                    isSaida = service.isFinalizar()
                    return@collect
                }

                is Resource.Loading -> {
                    println("Enviando...")
                }

                is Resource.Success -> {
                    println(result.data)
                    isSaida = service.isFinalizar()
                    return@collect
                }
            }
        }
//        return@runBlocking
    }
    return@runBlocking
}



