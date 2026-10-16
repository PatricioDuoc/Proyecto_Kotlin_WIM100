package com.example.appsemestral.validation

import com.example.appsemestral.model.CalculadoraPuntaje

/** Reglas de validación de la matriz de evaluación. */
object EvaluacionValidator {

    /** Puntajes iguales o menores a este valor exigen justificación escrita. */
    const val UMBRAL_JUSTIFICACION = 2
    const val JUSTIFICACION_MIN = 10
    const val OBSERVACION_CRITERIO_MAX = 300
    const val OBSERVACION_GENERAL_MIN = 20
    const val OBSERVACION_GENERAL_MAX = 400

    fun validarPuntaje(puntaje: Int?): ValidationResult = when {
        puntaje == null -> ValidationResult.error("Asigna un puntaje de 1 a 5")
        puntaje !in CalculadoraPuntaje.PUNTAJE_MINIMO..CalculadoraPuntaje.PUNTAJE_MAXIMO ->
            ValidationResult.error("El puntaje debe estar entre 1 y 5")
        else -> ValidationResult.Valido
    }

    fun validarObservacionCriterio(puntaje: Int?, observacion: String): ValidationResult {
        val largo = observacion.trim().length
        return when {
            largo > OBSERVACION_CRITERIO_MAX -> ValidationResult.error("Máximo $OBSERVACION_CRITERIO_MAX caracteres")
            puntaje != null && puntaje <= UMBRAL_JUSTIFICACION && largo < JUSTIFICACION_MIN ->
                ValidationResult.error("Justifica el puntaje bajo (mín. $JUSTIFICACION_MIN caracteres)")
            else -> ValidationResult.Valido
        }
    }

    fun validarObservacionGeneral(texto: String): ValidationResult {
        val largo = texto.trim().length
        return when {
            largo == 0 -> ValidationResult.error("La observación general es obligatoria")
            largo < OBSERVACION_GENERAL_MIN ->
                ValidationResult.error("Mínimo $OBSERVACION_GENERAL_MIN caracteres ($largo/$OBSERVACION_GENERAL_MIN)")
            largo > OBSERVACION_GENERAL_MAX -> ValidationResult.error("Máximo $OBSERVACION_GENERAL_MAX caracteres")
            else -> ValidationResult.Valido
        }
    }
}
