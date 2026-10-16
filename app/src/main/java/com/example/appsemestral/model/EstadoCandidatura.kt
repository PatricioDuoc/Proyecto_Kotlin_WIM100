package com.example.appsemestral.model

/** Estados por los que pasa una candidatura dentro del flujo WIM 100. */
enum class EstadoCandidatura(val etiqueta: String) {
    POSTULADA("Postulada"),
    EN_EVALUACION("En evaluación"),
    EVALUADA("Evaluada"),
    SELECCIONADA("Seleccionada"),
    NO_SELECCIONADA("No seleccionada");

    /** Una candidatura resuelta por Dirección ya no admite nuevas evaluaciones. */
    val estaResuelta: Boolean
        get() = this == SELECCIONADA || this == NO_SELECCIONADA
}
