package domain.models


import com.google.gson.annotations.SerializedName

data class Labirinto(
    @SerializedName("Completo")
    val completo: Boolean,
    @SerializedName("Dificuldade")
    val dificuldade: String,
    @SerializedName("Exploracao")
    val exploracao: Double,
    @SerializedName("IdLabirinto")
    val idLabirinto: Int,
    @SerializedName("Passos")
    val passos: Int
)