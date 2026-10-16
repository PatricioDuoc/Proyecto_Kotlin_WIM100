package com.example.appsemestral.ui.viewmodel

import com.example.appsemestral.validation.ValidationResult

/** Estado de un campo de formulario: su valor y el error (si lo hay). */
data class CampoState(
    val valor: String = "",
    val error: String? = null
) {
    val esValido: Boolean get() = error == null && valor.isNotBlank()

    fun con(valor: String, resultado: ValidationResult) = copy(valor = valor, error = resultado.mensaje)
}
