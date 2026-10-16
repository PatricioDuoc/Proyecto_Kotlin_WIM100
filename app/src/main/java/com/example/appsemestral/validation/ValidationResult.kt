package com.example.appsemestral.validation

/** Resultado de validar un campo: válido, o inválido con el mensaje que verá la usuaria. */
data class ValidationResult(
    val esValido: Boolean,
    val mensaje: String? = null
) {
    companion object {
        val Valido = ValidationResult(true)
        fun error(mensaje: String) = ValidationResult(false, mensaje)
    }
}
