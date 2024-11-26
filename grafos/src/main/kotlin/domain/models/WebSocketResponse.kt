package org.example.domain.models

data class VerticeInfo(
    val verticeAtual: Int,
    val tipo: Int,
    val adjacentes: List<Adjacente>,
)

data class Adjacente(
    val vertice: Int,
    val peso: Int
)

data class Caminho(val vertice: Int, val custoAcumulado: Int, val caminho: List<Int>)

