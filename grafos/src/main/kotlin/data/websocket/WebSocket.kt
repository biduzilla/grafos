package org.example.data.websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WebSocketClient(private val host: String, private val port: Int, private val path: String) {
    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val messageChannel = Channel<String>(Channel.UNLIMITED)
    private lateinit var session: DefaultWebSocketSession
    private var isConnected = false

    suspend fun connect() {
        client.webSocket(host = host, port = port, path = path) {
            session = this
            isConnected = true
            println("Connected to WebSocket")

            // Coroutine para escutar mensagens
            launch {
                for (message in incoming) {
                    when (message) {
                        is Frame.Text -> {
                            messageChannel.send("Received: ${message.readText()}")
                        }
                        is Frame.Binary -> {
                            messageChannel.send("Received binary frame of length: ${message.data.size}")
                        }
                        is Frame.Close -> {
                            messageChannel.send("Connection closing: ${message.readReason()}")
                            isConnected = false
                            break
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    suspend fun sendMessage(message: String) {
        if (isConnected) {
            session.send(Frame.Text(message))
        } else {
            println("Cannot send message, not connected to WebSocket.")
        }
    }

    fun receiveMessages(): Flow<String> = flow {
        while (true) {
            val receivedMessage = messageChannel.receive()
            emit(receivedMessage)
        }
    }

    fun close() {
        client.close()
        println("WebSocket connection closed")
    }
}
