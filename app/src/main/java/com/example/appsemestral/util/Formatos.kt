package com.example.appsemestral.util

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeChile: Locale = Locale.forLanguageTag("es-CL")

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm", localeChile).format(Date(millis))

fun formatearPuntaje(valor: Double?): String =
    if (valor == null) "—" else String.format(localeChile, "%.1f", valor)

fun iniciales(nombre: String): String =
    nombre.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

fun formatearTamano(bytes: Long?): String = when {
    bytes == null -> ""
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(localeChile, "%.1f MB", bytes / (1024.0 * 1024.0))
}

/** Obtiene la FragmentActivity desde un Context de Compose (necesaria para BiometricPrompt). */
fun Context.buscarFragmentActivity(): FragmentActivity? {
    var actual: Context = this
    while (actual is ContextWrapper) {
        if (actual is FragmentActivity) return actual
        actual = actual.baseContext
    }
    return null
}
