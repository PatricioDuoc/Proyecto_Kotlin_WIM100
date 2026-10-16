package com.example.appsemestral.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluacionValidatorTest {

    @Test
    fun puntaje_nulo_o_fuera_de_rango_es_invalido() {
        assertFalse(EvaluacionValidator.validarPuntaje(null).esValido)
        assertFalse(EvaluacionValidator.validarPuntaje(0).esValido)
        assertFalse(EvaluacionValidator.validarPuntaje(6).esValido)
        assertTrue(EvaluacionValidator.validarPuntaje(3).esValido)
    }

    @Test
    fun puntaje_bajo_exige_justificacion() {
        assertFalse(EvaluacionValidator.validarObservacionCriterio(2, "").esValido)
        assertTrue(EvaluacionValidator.validarObservacionCriterio(2, "Poca experiencia demostrable").esValido)
        assertTrue(EvaluacionValidator.validarObservacionCriterio(4, "").esValido)
    }

    @Test
    fun observacion_general_tiene_minimo() {
        assertFalse(EvaluacionValidator.validarObservacionGeneral("Muy bien").esValido)
        assertTrue(EvaluacionValidator.validarObservacionGeneral("Perfil sólido y con liderazgo comprobado").esValido)
    }
}
