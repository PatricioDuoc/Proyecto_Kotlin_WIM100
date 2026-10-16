package com.example.appsemestral.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.nativo.BiometriaHelper
import com.example.appsemestral.nativo.IntentsHelper
import com.example.appsemestral.nativo.NotificacionHelper
import com.example.appsemestral.ui.components.Avatar
import com.example.appsemestral.ui.components.EstadoChipTexto
import com.example.appsemestral.ui.components.TituloSeccion
import com.example.appsemestral.util.buscarFragmentActivity

/** Perfil de la sesión y panel de recursos nativos con el estado real de cada permiso. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    sesion: Sesion,
    mostrarMensaje: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    var permisoNotificaciones by remember { mutableStateOf(NotificacionHelper.tienePermiso(context)) }
    var rechazado by rememberSaveable { mutableStateOf(false) }
    var biometria by remember { mutableStateOf(BiometriaHelper.disponibilidad(context)) }

    // Al volver desde Ajustes se vuelve a consultar el estado real de los permisos
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permisoNotificaciones = NotificacionHelper.tienePermiso(context)
        biometria = BiometriaHelper.disponibilidad(context)
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoNotificaciones = concedido
        rechazado = !concedido
        mostrarMensaje(if (concedido) "Permiso concedido" else "Permiso rechazado: puedes activarlo desde Ajustes")
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Mi perfil") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(sesion.nombre, tamano = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(sesion.nombre, style = MaterialTheme.typography.titleLarge)
                        Text(sesion.rol.etiqueta, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(sesion.rol.descripcion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            TituloSeccion("Recursos nativos y permisos")

            TarjetaRecurso(
                icono = Icons.Filled.Notifications,
                titulo = "Notificaciones locales",
                descripcion = "Aviso al registrar una evaluación. Permiso POST_NOTIFICATIONS (en tiempo de ejecución desde Android 13).",
                estado = if (permisoNotificaciones) "Permitido" else "No permitido",
                activo = permisoNotificaciones
            ) {
                when {
                    permisoNotificaciones -> FilledTonalButton(onClick = {
                        val ok = NotificacionHelper.notificar(
                            context, 999, "Prueba WIM 100", "Las notificaciones funcionan correctamente."
                        )
                        if (!ok) mostrarMensaje("No se pudo mostrar la notificación")
                    }) { Text("Enviar notificación de prueba") }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !rechazado -> FilledTonalButton(onClick = {
                        lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text("Solicitar permiso") }

                    else -> OutlinedButton(onClick = {
                        if (!IntentsHelper.abrirAjustesNotificaciones(context)) mostrarMensaje("No se pudo abrir Ajustes")
                    }) { Text("Abrir ajustes de notificaciones") }
                }
            }

            TarjetaRecurso(
                icono = Icons.Filled.Fingerprint,
                titulo = "Biometría / bloqueo de pantalla",
                descripcion = "Verifica la identidad al ingresar. Permiso USE_BIOMETRIC (se concede al instalar).",
                estado = biometria.etiqueta,
                activo = biometria == BiometriaHelper.Disponibilidad.DISPONIBLE
            ) {
                if (biometria == BiometriaHelper.Disponibilidad.DISPONIBLE) {
                    FilledTonalButton(onClick = {
                        val activity = context.buscarFragmentActivity()
                        if (activity != null) {
                            BiometriaHelper.autenticar(
                                activity, "Prueba de verificación", sesion.nombre,
                                onExito = { mostrarMensaje("Identidad verificada correctamente") },
                                onError = { mostrarMensaje("Verificación no completada: $it") }
                            )
                        }
                    }) { Text("Probar verificación") }
                } else {
                    OutlinedButton(onClick = { IntentsHelper.abrirAjustesSeguridad(context) }) {
                        Text("Configurar bloqueo de pantalla")
                    }
                }
            }

            TarjetaRecurso(
                icono = Icons.Filled.FolderOpen,
                titulo = "Selector de documentos",
                descripcion = "Adjunta el CV en PDF con el selector del sistema. No requiere permisos de almacenamiento: solo se accede al archivo elegido.",
                estado = "Sin permiso requerido",
                activo = true
            )

            TarjetaRecurso(
                icono = Icons.Filled.Share,
                titulo = "Intents del sistema",
                descripcion = "Abrir el perfil profesional en el navegador, abrir el CV con otra app y compartir resultados.",
                estado = "Sin permiso requerido",
                activo = true
            )

            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onCerrarSesion,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
            Text(
                "WIM 100 · MVP académico DSY1105 · Solo datos ficticios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TarjetaRecurso(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    estado: String,
    activo: Boolean,
    acciones: @Composable ColumnScope.() -> Unit = {}
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                EstadoChipTexto(estado, activo)
            }
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            acciones()
        }
    }
}
