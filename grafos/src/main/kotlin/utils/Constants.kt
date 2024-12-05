package org.example.utils
object Constants{
    const val IP ="192.168.215.76"
    private const val URL = "apigrafos.onrender.com"
    const val BASE_URL ="https://$URL"
    const val BASE_URL_WS ="wss://apigrafos.onrender.com/"
    const val PORT ="8000"
//    const val BASE_URL ="http://$IP:$PORT"
    const val POST_GRUPO="${BASE_URL}/grupo"
    const val GET_ALL_GRUPOS="${BASE_URL}/grupos"
    const val INICIAR="${BASE_URL}/iniciar"
    const val GET_LABIRINTOS="${BASE_URL}/iniciar"
    const val GET_SESSOES="${BASE_URL}/sessoes"
    const val GENERATE_WS="${BASE_URL}/generate-websocket/"
    const val FINALIZAR_LABIRINTO="${BASE_URL}/resposta"
}
