package com.example.appsemestral.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.appsemestral.model.Rol
import com.example.appsemestral.nativo.BiometriaHelper
import com.example.appsemestral.ui.components.CampoValidado
import com.example.appsemestral.ui.components.sacudir
import com.example.appsemestral.util.buscarFragmentActivity
import com.example.appsemestral.ui.viewmodel.SesionViewModel

/** Ingreso: selección de perfil + nombre validado + verificación biométrica (recurso nativo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: SesionViewModel,
    mostrarMensaje: (String) -> Unit,
    onIngreso: () -> Unit
) {
    val estado by viewModel.loginState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val disponibilidad = remember { BiometriaHelper.disponibilidad(context) }

    val ingresar: () -> Unit = {
        if (viewModel.validar()) {
            val activity = context.buscarFragmentActivity()
            if (disponibilidad == BiometriaHelper.Disponibilidad.DISPONIBLE && activity != null) {
                BiometriaHelper.autenticar(
                    activity = activity,
                    titulo = "Verifica tu identidad",
                    subtitulo = "Ingreso como ${estado.rol.etiqueta}",
                    onExito = {
                        viewModel.iniciarSesion("verificación biométrica")
                        onIngreso()
                    },
                    onError = { mostrarMensaje("Verificación no completada: $it") }
                )
            } else {
                viewModel.iniciarSesion("sin verificación del dispositivo")
                mostrarMensaje("Dispositivo sin huella ni bloqueo: ingreso sin verificación")
                onIngreso()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EscudoAnimado()
            Spacer(Modifier.height(16.dp))
            Text(
                "WIM 100",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Postulación y evaluación · DYM Cyber",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp) // adaptable a tablets
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresa a tu perfil", style = MaterialTheme.typography.titleLarge)

                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        Rol.entries.forEachIndexed { indice, rol ->
                            SegmentedButton(
                                selected = estado.rol == rol,
                                onClick = { viewModel.onRol(rol) },
                                shape = SegmentedButtonDefaults.itemShape(indice, Rol.entries.size)
                            ) { Text(rol.etiqueta) }
                        }
                    }
                    Text(
                        estado.rol.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CampoValidado(
                        valor = estado.nombre.valor,
                        onValorChange = viewModel::onNombre,
                        etiqueta = if (estado.rol == Rol.EVALUADORA) "Nombre de la evaluadora" else "Nombre (Dirección)",
                        icono = Icons.Filled.Person,
                        error = estado.nombre.error,
                        placeholder = "Ej: Evaluadora Demo Tres",
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    )

                    Button(
                        onClick = ingresar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .sacudir(estado.intentosFallidos)
                    ) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ingresar")
                    }
                    Text(
                        "Verificación del dispositivo: ${disponibilidad.etiqueta}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "MVP académico · Solo datos ficticios",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Escudo con pulso infinito suave: identidad visual de seguridad. */
@Composable
private fun EscudoAnimado() {
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "escalaEscudo"
    )
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        modifier = Modifier
            .size(104.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}
