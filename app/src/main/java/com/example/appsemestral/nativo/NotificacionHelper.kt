package com.example.appsemestral.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appsemestral.MainActivity
import com.example.appsemestral.R

/**
 * RECURSO NATIVO: notificaciones locales.
 * Permiso: POST_NOTIFICATIONS, peligroso en Android 13+ (API 33), se pide en tiempo de ejecución.
 * Antes de notificar SIEMPRE se verifica el permiso; si no está, la app sigue funcionando.
 */
object NotificacionHelper {

    const val CANAL_ID = "wim100_evaluaciones"

    fun crearCanal(context: Context) {
        // Los canales existen desde Android 8 (API 26); en versiones anteriores no se necesitan
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.canal_notificaciones),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.canal_notificaciones_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun tienePermiso(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    /** @return true si la notificación se mostró. */
    @SuppressLint("MissingPermission") // el permiso se verifica en tienePermiso()
    fun notificar(context: Context, id: Int, titulo: String, texto: String): Boolean {
        if (!tienePermiso(context)) return false
        // Igual que tocar el ícono de la app: vuelve a la tarea existente sin perder la sesión
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
        return true
    }
}

/** Abstracción que usan los ViewModels: no dependen de Context ni de APIs de Android. */
interface Notificador {
    fun evaluacionRegistrada(candidataId: Long, nombre: String, puntaje: String): Boolean
}

class NotificadorAndroid(private val context: Context) : Notificador {
    override fun evaluacionRegistrada(candidataId: Long, nombre: String, puntaje: String): Boolean =
        NotificacionHelper.notificar(
            context,
            id = candidataId.toInt(),
            titulo = "Evaluación registrada",
            texto = "$nombre obtuvo $puntaje pts en la matriz WIM 100."
        )
}
