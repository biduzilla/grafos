package org.example.domain.models

import com.google.gson.annotations.SerializedName

data class GenerateWs(
    @SerializedName("grupo_id")
    val grupoId:String = "",
    @SerializedName("labirinto_id")
    val labirintoId:String = ""
)
