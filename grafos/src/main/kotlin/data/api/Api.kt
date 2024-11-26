package org.example.data.api

import domain.models.Labirintos
import domain.models.Sessoes
import org.example.domain.models.*
import org.example.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {
    @POST(Constants.POST_GRUPO)
    suspend fun postGrupo(@Body grupo: RequestGrupo): Response<ResponseGrupo>

    @GET("${Constants.INICIAR}/{Id}")
    suspend fun iniciar(@Path("Id") id:String): Response<IniciarResponse>

    @GET(Constants.GET_SESSOES)
    suspend fun sessoes(): Response<Sessoes>

    @GET("${Constants.GET_LABIRINTOS}/{Id}")
    suspend fun labirintos(@Path("Id") id:String): Response<Labirintos>

    @POST(Constants.GENERATE_WS)
    suspend fun generateWs(@Body gw:GenerateWs): Response<WebSocketUrl>

    @POST(Constants.FINALIZAR_LABIRINTO)
    suspend fun enviarReposta(@Body resposta: Resposta): Response<Message>
}