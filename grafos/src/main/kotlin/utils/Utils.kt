package org.example.utils

import com.google.gson.Gson
import org.example.domain.models.Adjacente
import org.example.domain.models.VerticeInfo

fun messageToVerticeInfo(message: String): VerticeInfo {
    val gson = Gson()
    val verticeAtualRegex = "Vértice atual: (\\d+)".toRegex()
    val tipoRegex = "Tipo: (\\d+)".toRegex()
    val adjacentesRegex = "\\((\\d+), (\\d+)\\)".toRegex()

    val verticeAtual = verticeAtualRegex.find(message)?.groupValues?.get(1)?.toInt() ?: 0
    val tipo = tipoRegex.find(message)?.groupValues?.get(1)?.toInt() ?: 0

    val adjacentes = adjacentesRegex.findAll(message).map {
        val vertice = it.groupValues[1].toInt()
        val peso = it.groupValues[2].toInt()
        Adjacente(vertice, peso)
    }.toList()

    return VerticeInfo(
        verticeAtual = verticeAtual,
        tipo = tipo,
        adjacentes = adjacentes
    )
}