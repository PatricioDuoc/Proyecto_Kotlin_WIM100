package com.example.appsemestral.model

/**
 * Regla de negocio del puntaje WIM:
 * promedio ponderado de puntajes 1..5, expresado en escala 0..100.
 */
object CalculadoraPuntaje {
    const val PUNTAJE_MINIMO = 1
    const val PUNTAJE_MAXIMO = 5

    fun calcular(criterios: List<Criterio>, puntajes: Map<Long, Int>): Double {
        val pesoTotal = criterios.sumOf { it.peso }
        if (pesoTotal == 0) return 0.0
        val obtenido = criterios.sumOf { (puntajes[it.id] ?: 0) * it.peso }
        return obtenido.toDouble() / (pesoTotal * PUNTAJE_MAXIMO) * 100.0
    }

    fun categoria(puntaje: Double): String = when {
        puntaje >= 85 -> "Sobresaliente"
        puntaje >= 70 -> "Sólida"
        puntaje >= 50 -> "En desarrollo"
        puntaje > 0 -> "Inicial"
        else -> "Sin puntaje"
    }

    fun nivel(puntaje: Int?): String = when (puntaje) {
        1 -> "Insuficiente"
        2 -> "Básico"
        3 -> "Adecuado"
        4 -> "Destacado"
        5 -> "Sobresaliente"
        else -> "Sin asignar"
    }
}
