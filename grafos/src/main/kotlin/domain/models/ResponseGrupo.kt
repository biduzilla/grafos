package org.example.domain.models

import com.google.gson.annotations.SerializedName

data class ResponseGrupo(
    @SerializedName("GrupoId")
    val grupoId: String = ""
)
