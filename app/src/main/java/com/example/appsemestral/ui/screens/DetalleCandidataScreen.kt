package com.example.appsemestral.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.DetallePuntaje
import com.example.appsemestral.model.EstadoCandidatura
import com.example.appsemestral.model.Evaluacion
import com.example.appsemestral.model.Rol
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.nativo.IntentsHelper
import com.example.appsemestral.ui.components.Avatar
import com.example.appsemestral.ui.components.BannerInfo
import com.example.appsemestral.ui.components.EstadoChip
import com.example.appsemestral.ui.components.PuntajeCircular
import com.example.appsemestral.ui.components.TituloSeccion
import com.example.appsemestral.ui.components.VisorCvDialog
import com.example.appsemestral.ui.components.colorPuntaje
import com.example.appsemestral.util.formatearFecha
import com.example.appsemestral.util.formatearPuntaje
import com.example.appsemestral.ui.viewmodel.AppViewModelProvider
import com.example.appsemestral.ui.viewmodel.DetalleCandidataViewModel
import com.example.appsemestral.ui.viewmodel.DetalleUiState

/** Detalle: antecedentes (CV, perfil), evaluaciones, acciones por rol y trazabilidad. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCandidataScreen(
    sesion: Sesion,
    onVolver: () -> Unit,
    onEvaluar: (Long) -> Unit,
    mostrarMensaje: (String) -> Unit,
    viewModel: DetalleCandidataViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var verCv by rememberSaveable { mutableStateOf(false) }
    var resolucionPendiente by remember { mutableStateOf<EstadoCandidatura?>(null) }
    val candidata = estado.candidata

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(candidata?.nombre ?: "Candidatura", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (candidata != null) {
                        // RECURSO NATIVO: intent de compartir
                        IconButton(onClick = {
                            val ok = IntentsHelper.compartirTexto(context, "Candidatura WIM 100", viewModel.textoResumen())
                            if (ok) viewModel.registrarAccion(Acciones.COMPARTIR, "Resumen de ${candidata.nombre}", sesion.nombre)
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir resumen")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (candidata == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (estado.cargando) CircularProgressIndicator() else Text("Candidatura no encontrada")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Encabezado(candidata, estado) }
                item {
                    Antecedentes(
                        candidata = candidata,
                        onVerCv = {
                            verCv = true
                            viewModel.registrarAccion(Acciones.CONSULTA_CV, candidata.cvNombre ?: "CV", sesion.nombre)
                        },
                        onAbrirPerfil = {
                            // RECURSO NATIVO: intent para abrir el enlace en el navegador
                            if (IntentsHelper.abrirEnlace(context, candidata.linkedinUrl)) {
                                viewModel.registrarAccion(Acciones.CONSULTA_PERFIL, candidata.linkedinUrl, sesion.nombre)
                            } else {
                                mostrarMensaje("No hay un navegador disponible para abrir el enlace")
                            }
                        }
                    )
                }
                item {
                    AccionesPorRol(
                        sesion = sesion,
                        candidata = candidata,
                        estado = estado,
                        onEvaluar = { onEvaluar(candidata.id) },
                        onResolver = { resolucionPendiente = it }
                    )
                }

                item { TituloSeccion("Evaluaciones (${estado.evaluaciones.size})") }
                if (estado.evaluaciones.isEmpty()) {
                    item { Text("Aún no hay evaluaciones registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(estado.evaluaciones, key = { it.id }) { evaluacion ->
                    TarjetaEvaluacion(evaluacion, estado.puntajes.filter { it.evaluacionId == evaluacion.id })
                }

                item { TituloSeccion("Trazabilidad") }
                items(estado.historial, key = { "h${it.id}" }) { registro ->
                    ItemHistorial(registro)
                }
            }
        }
    }

    if (verCv && candidata != null) {
        VisorCvDialog(
            uri = candidata.cvUri,
            nombre = candidata.cvNombre,
            onCerrar = { verCv = false },
            onAbrirExterno = {
                val uri = candidata.cvUri
                if (uri == null || !IntentsHelper.abrirPdf(context, uri)) {
                    mostrarMensaje("No hay una app instalada para abrir PDF; usa el visor integrado")
                }
            }
        )
    }

    val pendiente = resolucionPendiente
    if (pendiente != null && candidata != null) {
        AlertDialog(
            onDismissRequest = { resolucionPendiente = null },
            icon = { Icon(Icons.Filled.Gavel, contentDescription = null) },
            title = { Text("Confirmar resolución") },
            text = { Text("¿Marcar a ${candidata.nombre} como \"${pendiente.etiqueta}\"? Esta acción queda registrada en el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resolver(pendiente, sesion.nombre)
                    mostrarMensaje("Candidatura ${pendiente.etiqueta.lowercase()}")
                    resolucionPendiente = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { resolucionPendiente = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Encabezado(candidata: Candidata, estado: DetalleUiState) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(candidata.nombre, tamano = 64.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(candidata.nombre, style = MaterialTheme.typography.titleLarge)
                Text(candidata.area, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EstadoChip(candidata.estado)
                Text(
                    "Registrada el ${formatearFecha(candidata.fechaRegistro)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            estado.promedio?.let { PuntajeCircular(it, tamano = 72.dp) }
        }
    }
}

@Composable
private fun Antecedentes(candidata: Candidata, onVerCv: () -> Unit, onAbrirPerfil: () -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Antecedentes", style = MaterialTheme.typography.titleMedium)
            Dato(Icons.Filled.Email, candidata.correo)
            Dato(Icons.Filled.Phone, candidata.telefono)
            Dato(Icons.Filled.Work, "${candidata.aniosExperiencia} años de experiencia")
            Text(candidata.resumenExperiencia, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onVerCv, enabled = candidata.cvUri != null, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ver CV")
                }
                OutlinedButton(onClick = onAbrirPerfil, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Link, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Perfil", maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun Dato(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Las acciones disponibles dependen del rol y del estado (estructuras de control en la UI). */
@Composable
private fun AccionesPorRol(
    sesion: Sesion,
    candidata: Candidata,
    estado: DetalleUiState,
    onEvaluar: () -> Unit,
    onResolver: (EstadoCandidatura) -> Unit
) {
    when (sesion.rol) {
        Rol.EVALUADORA -> when {
            candidata.estado.estaResuelta -> BannerInfo(
                "Candidatura resuelta (${candidata.estado.etiqueta}). No admite nuevas evaluaciones.",
                icono = Icons.Filled.Lock
            )
            estado.yaEvaluo(sesion.nombre) -> BannerInfo(
                "Ya registraste tu evaluación. El sistema impide evaluaciones duplicadas.",
                icono = Icons.Filled.CheckCircle
            )
            else -> Button(onClick = onEvaluar, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Evaluar con matriz WIM")
            }
        }

        Rol.DIRECCION -> when {
            candidata.estado == EstadoCandidatura.EVALUADA -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onResolver(EstadoCandidatura.NO_SELECCIONADA) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("No seleccionar")
                }
                Button(onClick = { onResolver(EstadoCandidatura.SELECCIONADA) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Seleccionar")
                }
            }
            candidata.estado.estaResuelta -> BannerInfo(
                "Resolución registrada: ${candidata.estado.etiqueta}.",
                icono = Icons.Filled.Gavel
            )
            else -> BannerInfo(
                "Para resolver se requieren al menos ${Evaluacion.MINIMO_PARA_CONSOLIDAR} evaluaciones " +
                    "(${estado.evaluaciones.size}/${Evaluacion.MINIMO_PARA_CONSOLIDAR}).",
                icono = Icons.Filled.Lock
            )
        }
    }
}

/** Tarjeta expandible con el detalle por criterio (animateContentSize + rotación del ícono). */
@Composable
private fun TarjetaEvaluacion(evaluacion: Evaluacion, puntajes: List<DetallePuntaje>) {
    var expandida by rememberSaveable(evaluacion.id) { mutableStateOf(false) }
    val rotacion by animateFloatAsState(if (expandida) 180f else 0f, label = "flecha")

    OutlinedCard(onClick = { expandida = !expandida }, modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(evaluacion.evaluadora, style = MaterialTheme.typography.titleSmall)
                    Text(formatearFecha(evaluacion.fecha), style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    "${formatearPuntaje(evaluacion.puntajeTotal)} pts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorPuntaje(evaluacion.puntajeTotal)
                )
                Icon(Icons.Filled.ExpandMore, contentDescription = "Ver detalle", modifier = Modifier.rotate(rotacion))
            }
            Text(evaluacion.observacionGeneral, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            AnimatedVisibility(visible = expandida) {
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider()
                    puntajes.forEach { p ->
                        Row {
                            Text("${p.criterioNombre} (${p.peso}%)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("${p.puntaje}/5", fontWeight = FontWeight.SemiBold)
                        }
                        if (p.observacion.isNotBlank()) {
                            Text(
                                "“${p.observacion}”",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
