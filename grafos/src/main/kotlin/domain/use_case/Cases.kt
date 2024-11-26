package org.example.domain.use_case

import domain.models.Labirintos
import domain.models.Sessoes
import kotlinx.coroutines.flow.Flow
import org.example.domain.models.*
import org.example.utils.Resource

class Cases {
    fun postGrupo(nome: String): Flow<Resource<ResponseGrupo>> {
        val grupo = RequestGrupo(nome = nome)
        val useCasePostGrupo = UseCasePostGrupo()
        return useCasePostGrupo(grupo)
    }

    fun iniciar(id: String): Flow<Resource<IniciarResponse>> {
        val userCaseIniciar = UseCaseIniciar()
        return userCaseIniciar(id)
    }

    fun getSessoes(): Flow<Resource<Sessoes>> {
        val useCaseSessoes = UseCaseSessoes()
        return useCaseSessoes()
    }

    fun getLabirintos(id: String): Flow<Resource<Labirintos>> {
        val useCaseLabirintos = UseCaseGetLabirintos()
        return useCaseLabirintos(id)
    }

    fun generateWs(gw: GenerateWs): Flow<Resource<WebSocketUrl>> {
        val useCaseGs = UseCaseGenerateWs()
        return useCaseGs(gw)
    }

    fun enviarResposta(resposta: Resposta): Flow<Resource<Message>> {
        println(resposta)
        val useCase = UseCaseEnviarResposta()
        return useCase(resposta)
    }
}