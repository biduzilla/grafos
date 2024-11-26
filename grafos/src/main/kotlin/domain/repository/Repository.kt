package org.example.domain.repository

import domain.models.Labirintos
import domain.models.Sessoes
import org.example.domain.models.*
import retrofit2.Response
import retrofit2.http.Body

interface Repository {
    suspend fun postGrupo(grupo: RequestGrupo): Response<ResponseGrupo>

    suspend fun iniciar(id: String): Response<IniciarResponse>

    suspend fun getSessoes(): Response<Sessoes>

    suspend fun getLabirintos(id: String): Response<Labirintos>

    suspend fun generateWs(gw: GenerateWs): Response<WebSocketUrl>

    suspend fun enviarReposta(resposta: Resposta): Response<Message>
}