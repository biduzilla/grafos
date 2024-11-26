package org.example.domain.models

import com.google.gson.annotations.SerializedName

data class IniciarResponse(
    @SerializedName("Conexao")
    val conexao: String = ""
)
