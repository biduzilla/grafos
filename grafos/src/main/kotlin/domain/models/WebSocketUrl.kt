package org.example.domain.models

import com.google.gson.annotations.SerializedName

data class WebSocketUrl(
    @SerializedName("websocket_url")
    val websocketUrl:String = ""
)
