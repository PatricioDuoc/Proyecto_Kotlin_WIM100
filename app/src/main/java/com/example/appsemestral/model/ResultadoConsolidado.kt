package com.example.appsemestral.model

/** Fila del ranking consolidado (resultado de un GROUP BY en Room). */
data class ResultadoConsolidado(
    val candidataId: Long,
    val nombre: String,
    val area: String,
    val estado: EstadoCandidatura,
    val totalEvaluaciones: Int,
    val promedio: Double?
)
