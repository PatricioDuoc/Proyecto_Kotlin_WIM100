package com.example.appsemestral.nativo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * RECURSO NATIVO: intents implícitos del sistema (navegador, visor PDF, compartir, ajustes).
 * No requieren permisos; se controla el caso en que no exista una app que los atienda.
 */
object IntentsHelper {

    fun abrirEnlace(context: Context, url: String): Boolean =
        iniciar(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun abrirPdf(context: Context, uri: String): Boolean =
        iniciar(
            context,
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uri), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )

    fun compartirTexto(context: Context, titulo: String, texto: String): Boolean {
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        return iniciar(context, Intent.createChooser(envio, titulo))
    }

    fun abrirAjustesNotificaciones(context: Context): Boolean {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }
        return iniciar(context, intent)
    }

    fun abrirAjustesSeguridad(context: Context): Boolean =
        iniciar(context, Intent(Settings.ACTION_SECURITY_SETTINGS))

    private fun iniciar(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
