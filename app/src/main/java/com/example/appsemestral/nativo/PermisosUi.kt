package com.example.appsemestral.nativo

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val PREFS = "wim100_prefs"
private const val CLAVE_SOLICITADO = "permiso_notificaciones_solicitado"

/**
 * Gestión del permiso POST_NOTIFICATIONS (Android 13+):
 * 1) se explica primero por qué se necesita (diálogo propio),
 * 2) luego se lanza el diálogo del sistema,
 * 3) si se rechaza, la app sigue funcionando y se puede activar desde Perfil.
 * Solo se pregunta automáticamente una vez.
 */
@Composable
fun SolicitudPermisoNotificaciones(mostrarMensaje: (String) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var mostrarDialogo by remember {
        mutableStateOf(!NotificacionHelper.tienePermiso(context) && !prefs.getBoolean(CLAVE_SOLICITADO, false))
    }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        mostrarMensaje(
            if (concedido) "Notificaciones activadas"
            else "Notificaciones desactivadas. Puedes activarlas desde Perfil."
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            title = { Text("¿Activar notificaciones?") },
            text = { Text("WIM 100 te avisará cuando se registre una evaluación. Es opcional: la app funciona igual sin este permiso.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    prefs.edit().putBoolean(CLAVE_SOLICITADO, true).apply()
                    lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    prefs.edit().putBoolean(CLAVE_SOLICITADO, true).apply()
                }) { Text("Ahora no") }
            }
        )
    }
}
