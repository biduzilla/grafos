package org.example.data.repository

import domain.models.Labirintos
import domain.models.Sessoes
import org.example.data.RetrofitInstance
import org.example.domain.models.*
import org.example.domain.repository.Repository
import retrofit2.Response

class RepositoryImpl : Repository {
    override suspend fun postGrupo(grupo: RequestGrupo): Response<ResponseGrupo> {
        return RetrofitInstance.api.postGrupo(grupo)
    }

    override suspend fun iniciar(id: String): Response<IniciarResponse> {
        return RetrofitInstance.api.iniciar(id)
    }

    override suspend fun getSessoes(): Response<Sessoes> {
        return RetrofitInstance.api.sessoes()
    }

    override suspend fun getLabirintos(id: String): Response<Labirintos> {
        return RetrofitInstance.api.labirintos(id)
    }

    override suspend fun generateWs(gw: GenerateWs): Response<WebSocketUrl> {
        return RetrofitInstance.api.generateWs(gw)
    }

    override suspend fun enviarReposta(resposta: Resposta): Response<Message> {
        return RetrofitInstance.api.enviarReposta(resposta)
    }
}