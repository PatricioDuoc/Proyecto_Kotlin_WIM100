package com.example.appsemestral.model

/** Perfiles priorizados para el MVP. */
enum class Rol(val etiqueta: String, val descripcion: String) {
    EVALUADORA(
        "Evaluadora",
        "Registra candidaturas y las evalúa con la matriz WIM."
    ),
    DIRECCION(
        "Dirección",
        "Consulta resultados consolidados y resuelve candidaturas evaluadas."
    )
}

/** Sesión activa: identifica a la persona que realiza cada acción (trazabilidad). */
data class Sesion(
    val nombre: String,
    val rol: Rol
)
