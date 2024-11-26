package domain.models

import com.google.gson.annotations.SerializedName

data class Sessao(
    @SerializedName("Conexao")
    val conexao: String,
    @SerializedName("IdGrupo")
    val idGrupo: String
)