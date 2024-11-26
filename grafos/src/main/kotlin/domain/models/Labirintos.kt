package domain.models


import com.google.gson.annotations.SerializedName

data class Labirintos(
    @SerializedName("Labirintos")
    val labirintos: List<Labirinto>
)