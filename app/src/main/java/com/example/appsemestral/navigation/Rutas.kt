package com.example.appsemestral.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

object Rutas {
    const val ARG_ID = "candidataId"

    const val LOGIN = "login"
    const val CANDIDATAS = "candidatas"
    const val RESULTADOS = "resultados"
    const val HISTORIAL = "historial"
    const val PERFIL = "perfil"
    const val NUEVA_CANDIDATA = "nueva-candidata"
    const val DETALLE = "candidata/{$ARG_ID}"
    const val EVALUAR = "evaluar/{$ARG_ID}"

    fun detalle(id: Long) = "candidata/$id"
    fun evaluar(id: Long) = "evaluar/$id"
}

/** Destinos de la barra de navegación inferior. */
enum class DestinoPrincipal(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    CANDIDATAS(Rutas.CANDIDATAS, "Candidatas", Icons.Filled.People),
    RESULTADOS(Rutas.RESULTADOS, "Resultados", Icons.Filled.Leaderboard),
    HISTORIAL(Rutas.HISTORIAL, "Historial", Icons.Filled.History),
    PERFIL(Rutas.PERFIL, "Perfil", Icons.Filled.AccountCircle)
}
