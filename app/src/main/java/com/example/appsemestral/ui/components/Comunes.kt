package com.example.appsemestral.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import com.example.appsemestral.model.EstadoCandidatura
import com.example.appsemestral.ui.theme.AmbarAviso
import com.example.appsemestral.ui.theme.VerdeExito
import com.example.appsemestral.ui.theme.VerdeExitoContenedor
import com.example.appsemestral.util.iniciales
import kotlin.math.roundToInt

/** Círculo con iniciales de la persona. */
@Composable
fun Avatar(nombre: String, modifier: Modifier = Modifier, tamano: Dp = 48.dp) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            iniciales(nombre),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            style = if (tamano > 56.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall
        )
    }
}

/** Etiqueta de estado con color animado cuando la candidatura cambia de estado. */
@Composable
fun EstadoChip(estado: EstadoCandidatura, modifier: Modifier = Modifier) {
    val esquema = MaterialTheme.colorScheme
    val (fondoObjetivo, textoObjetivo) = when (estado) {
        EstadoCandidatura.POSTULADA -> esquema.surfaceVariant to esquema.onSurfaceVariant
        EstadoCandidatura.EN_EVALUACION -> esquema.tertiaryContainer to esquema.onTertiaryContainer
        EstadoCandidatura.EVALUADA -> esquema.secondaryContainer to esquema.onSecondaryContainer
        EstadoCandidatura.SELECCIONADA -> VerdeExitoContenedor to VerdeExito
        EstadoCandidatura.NO_SELECCIONADA -> esquema.errorContainer to esquema.onErrorContainer
    }
    val fondo by animateColorAsState(fondoObjetivo, tween(500), label = "fondoEstado")
    val texto by animateColorAsState(textoObjetivo, tween(500), label = "textoEstado")
    Surface(shape = RoundedCornerShape(50), color = fondo, modifier = modifier) {
        Text(
            estado.etiqueta,
            color = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Indicador circular del puntaje (0-100) con animación de progreso, número y color. */
@Composable
fun PuntajeCircular(valor: Double, modifier: Modifier = Modifier, tamano: Dp = 88.dp) {
    val progreso by animateFloatAsState(
        targetValue = (valor / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progresoPuntaje"
    )
    val numero by animateIntAsState(valor.roundToInt(), tween(700), label = "numeroPuntaje")
    val color by animateColorAsState(colorPuntaje(valor), tween(500), label = "colorPuntaje")
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progreso },
            modifier = Modifier.size(tamano),
            color = color,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$numero", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("/ 100", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun colorPuntaje(valor: Double): Color = when {
    valor >= 85 -> VerdeExito
    valor >= 70 -> MaterialTheme.colorScheme.secondary
    valor >= 50 -> AmbarAviso
    valor > 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.outline
}

/** Pantalla vacía amigable (sin resultados, sin datos). */
@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, descripcion: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Banner informativo (avisos de reglas de negocio). */
@Composable
fun BannerInfo(
    texto: String,
    modifier: Modifier = Modifier,
    icono: ImageVector = Icons.Filled.Info,
    fondo: Color = MaterialTheme.colorScheme.secondaryContainer,
    contenido: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(color = fondo, shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = contenido)
            Spacer(Modifier.width(12.dp))
            Text(texto, color = contenido, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

/**
 * Animación de "sacudida" horizontal: se dispara cada vez que cambia [disparador]
 * (por ejemplo, el número de intentos fallidos al enviar un formulario).
 */
fun Modifier.sacudir(disparador: Int): Modifier = composed {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            for (x in listOf(-14f, 14f, -10f, 10f, -5f, 5f, 0f)) {
                desplazamiento.animateTo(x, tween(durationMillis = 45))
            }
        }
    }
    this.offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
}

/** Chip simple verde/rojo para estados de permisos. */
@Composable
fun EstadoChipTexto(texto: String, activo: Boolean, modifier: Modifier = Modifier) {
    val fondo by animateColorAsState(
        if (activo) VerdeExitoContenedor else MaterialTheme.colorScheme.errorContainer,
        label = "fondoChip"
    )
    Surface(shape = RoundedCornerShape(50), color = fondo, modifier = modifier) {
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = if (activo) VerdeExito else MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
