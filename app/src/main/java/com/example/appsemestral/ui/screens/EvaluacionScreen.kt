package com.example.appsemestral.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.model.CalculadoraPuntaje
import com.example.appsemestral.model.Criterio
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.ui.components.BannerInfo
import com.example.appsemestral.ui.components.CampoValidado
import com.example.appsemestral.ui.components.EstadoVacio
import com.example.appsemestral.ui.components.MensajeError
import com.example.appsemestral.ui.components.PuntajeCircular
import com.example.appsemestral.ui.components.sacudir
import com.example.appsemestral.util.formatearPuntaje
import com.example.appsemestral.validation.EvaluacionValidator
import com.example.appsemestral.ui.viewmodel.AppViewModelProvider
import com.example.appsemestral.ui.viewmodel.EvaluacionViewModel
import com.example.appsemestral.ui.viewmodel.EvaluacionUiState

/** Aplicación de la matriz de criterios WIM con puntaje ponderado en vivo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluacionScreen(
    sesion: Sesion,
    onVolver: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    viewModel: EvaluacionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(sesion.nombre) { viewModel.cargar(sesion.nombre) }
    LaunchedEffect(estado.puntajeGuardado) {
        estado.puntajeGuardado?.let {
            mostrarMensaje("Evaluación registrada · ${formatearPuntaje(it)} pts")
            onVolver()
        }
    }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            mostrarMensaje(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Matriz de evaluación WIM")
                        Text(
                            estado.candidata?.nombre.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // AnimatedContent: transición suave entre cargando / bloqueado / formulario
        AnimatedContent(
            targetState = when {
                estado.cargando -> 0
                estado.candidata == null -> 1
                estado.yaEvaluada -> 2
                else -> 3
            },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "contenidoEvaluacion",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { pantalla ->
            when (pantalla) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                1 -> EstadoVacio(Icons.Filled.Block, "Candidatura no encontrada", "Vuelve a la lista e inténtalo otra vez.")
                2 -> EvaluacionBloqueada(onVolver)
                else -> FormularioMatriz(
                    estado = estado,
                    evaluadora = sesion.nombre,
                    onPuntaje = viewModel::onPuntaje,
                    onObservacion = viewModel::onObservacion,
                    onObservacionGeneral = viewModel::onObservacionGeneral,
                    onEnviar = { if (viewModel.validar()) confirmar = true }
                )
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            icon = { Icon(Icons.Filled.HowToReg, contentDescription = null) },
            title = { Text("Confirmar evaluación") },
            text = {
                Text(
                    "Puntaje final: ${formatearPuntaje(estado.puntajeTotal)} pts " +
                        "(${CalculadoraPuntaje.categoria(estado.puntajeTotal)}).\n\n" +
                        "Una vez registrada no podrás modificarla ni volver a evaluar a esta candidata."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.guardar(sesion.nombre)
                }) { Text("Registrar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Revisar") } }
        )
    }
}

@Composable
private fun FormularioMatriz(
    estado: EvaluacionUiState,
    evaluadora: String,
    onPuntaje: (Long, Int) -> Unit,
    onObservacion: (Long, String) -> Unit,
    onObservacionGeneral: (String) -> Unit,
    onEnviar: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ResumenPuntaje(estado, evaluadora) }
        item {
            Text(
                "Matriz provisional, sujeta a validación del Socio Formador. Escala: 1 Insuficiente · 5 Sobresaliente.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(estado.criterios, key = { it.id }) { criterio ->
            TarjetaCriterio(
                criterio = criterio,
                puntaje = estado.puntajes[criterio.id],
                observacion = estado.observaciones[criterio.id].orEmpty(),
                errorPuntaje = estado.erroresPuntaje[criterio.id],
                errorObservacion = estado.erroresObservacion[criterio.id],
                onPuntaje = { onPuntaje(criterio.id, it) },
                onObservacion = { onObservacion(criterio.id, it) }
            )
        }
        item {
            CampoValidado(
                valor = estado.observacionGeneral,
                onValorChange = onObservacionGeneral,
                etiqueta = "Observación general",
                icono = Icons.Filled.EditNote,
                error = estado.errorObservacionGeneral,
                singleLine = false,
                minLines = 3,
                maxLength = EvaluacionValidator.OBSERVACION_GENERAL_MAX,
                imeAction = ImeAction.Default
            )
        }
        item {
            Button(
                onClick = onEnviar,
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .sacudir(estado.intentosFallidos)
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.HowToReg, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar evaluación")
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun ResumenPuntaje(estado: EvaluacionUiState, evaluadora: String) {
    val progreso by animateFloatAsState(
        targetValue = if (estado.criterios.isEmpty()) 0f else estado.completados / estado.criterios.size.toFloat(),
        label = "avanceMatriz"
    )
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PuntajeCircular(estado.puntajeTotal)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Puntaje ponderado", style = MaterialTheme.typography.labelLarge)
                AnimatedContent(targetState = CalculadoraPuntaje.categoria(estado.puntajeTotal), label = "categoria") { categoria ->
                    Text(categoria, style = MaterialTheme.typography.titleLarge)
                }
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
                Text(
                    "${estado.completados} de ${estado.criterios.size} criterios · Evaluadora: $evaluadora",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun TarjetaCriterio(
    criterio: Criterio,
    puntaje: Int?,
    observacion: String,
    errorPuntaje: String?,
    errorObservacion: String?,
    onPuntaje: (Int) -> Unit,
    onObservacion: (String) -> Unit
) {
    val hayError = errorPuntaje != null || errorObservacion != null
    val colorBorde by animateColorAsState(
        if (hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
        label = "bordeCriterio"
    )
    val requiereJustificacion = puntaje != null && puntaje <= EvaluacionValidator.UMBRAL_JUSTIFICACION

    OutlinedCard(border = BorderStroke(1.dp, colorBorde), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(criterio.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        "Peso ${criterio.peso}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(criterio.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SelectorPuntaje(puntaje = puntaje, onPuntaje = onPuntaje)
            Text(
                CalculadoraPuntaje.nivel(puntaje),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            MensajeError(errorPuntaje)

            CampoValidado(
                valor = observacion,
                onValorChange = onObservacion,
                etiqueta = if (requiereJustificacion) "Justificación (obligatoria)" else "Observación (opcional)",
                icono = Icons.Filled.Edit,
                error = errorObservacion,
                singleLine = false,
                maxLength = EvaluacionValidator.OBSERVACION_CRITERIO_MAX,
                imeAction = ImeAction.Default,
                mostrarValido = false
            )
        }
    }
}

/** Selector 1..5 con rebote (spring) y cambio de color al seleccionar. */
@Composable
private fun SelectorPuntaje(puntaje: Int?, onPuntaje: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        for (n in CalculadoraPuntaje.PUNTAJE_MINIMO..CalculadoraPuntaje.PUNTAJE_MAXIMO) {
            val seleccionado = puntaje == n
            val escala by animateFloatAsState(
                targetValue = if (seleccionado) 1.18f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "escalaPuntaje$n"
            )
            val fondo by animateColorAsState(
                if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "fondoPuntaje$n"
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                    }
                    .clip(CircleShape)
                    .background(fondo)
                    .clickable { onPuntaje(n) }
                    .semantics { contentDescription = "Puntaje $n de 5" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$n",
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EvaluacionBloqueada(onVolver: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BannerInfo(
            "Ya registraste una evaluación para esta candidata. Para resguardar la objetividad, " +
                "cada evaluadora puede evaluar una sola vez.",
            icono = Icons.Filled.Block,
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onVolver) { Text("Volver al detalle") }
    }
}
