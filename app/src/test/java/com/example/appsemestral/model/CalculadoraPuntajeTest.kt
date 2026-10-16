package com.example.appsemestral.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraPuntajeTest {

    private val criterios = listOf(
        Criterio(id = 1, nombre = "A", descripcion = "", peso = 60, orden = 1),
        Criterio(id = 2, nombre = "B", descripcion = "", peso = 40, orden = 2)
    )

    @Test
    fun todo_en_cinco_da_cien() {
        assertEquals(100.0, CalculadoraPuntaje.calcular(criterios, mapOf(1L to 5, 2L to 5)), 0.001)
    }

    @Test
    fun promedio_ponderado() {
        // (5*60 + 3*40) / (100*5) * 100 = 84
        assertEquals(84.0, CalculadoraPuntaje.calcular(criterios, mapOf(1L to 5, 2L to 3)), 0.001)
    }

    @Test
    fun sin_criterios_da_cero() {
        assertEquals(0.0, CalculadoraPuntaje.calcular(emptyList(), emptyMap()), 0.001)
    }

    @Test
    fun clave_de_evaluadora_se_normaliza() {
        assertEquals("ana pérez", Evaluacion.normalizarClave("  Ana   Pérez "))
    }
}
