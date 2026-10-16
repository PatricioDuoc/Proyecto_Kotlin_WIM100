package com.example.appsemestral.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.model.Evaluacion
import com.example.appsemestral.model.ResultadoConsolidado
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.nativo.IntentsHelper
import com.example.appsemestral.ui.components.EstadoChip
import com.example.appsemestral.ui.components.EstadoVacio
import com.example.appsemestral.ui.components.colorPuntaje
import com.example.appsemestral.util.formatearPuntaje
import com.example.appsemestral.ui.viewmodel.AppViewModelProvider
import com.example.appsemestral.ui.viewmodel.ResultadosViewModel

/** Resultados consolidados: indicadores generales y ranking por puntaje promedio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosScreen(
    sesion: Sesion,
    onAbrir: (Long) -> Unit,
    viewModel: ResultadosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados consolidados") },
                actions = {
                    IconButton(onClick = {
                        if (IntentsHelper.compartirTexto(context, "Resultados WIM 100", viewModel.textoCompartir())) {
                            viewModel.registrarCompartir(sesion.nombre)
                        }
                    }) { Icon(Icons.Filled.Share, contentDescription = "Compartir ranking") }
                }
            )
        }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Indicador("Candidaturas", estado.totalCandidatas.toString(), Icons.Filled.People, Modifier.weight(1f))
                        Indicador("Con evaluación", estado.conEvaluacion.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f))
                        Indicador("Promedio", formatearPuntaje(estado.promedioGeneral), Icons.Filled.Insights, Modifier.weight(1f))
                    }
                }
                item {
                    Column {
                        Text("Ranking por puntaje promedio", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Una candidatura queda \"Evaluada\" con al menos ${Evaluacion.MINIMO_PARA_CONSOLIDAR} evaluaciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (estado.resultados.isEmpty()) {
                    item { EstadoVacio(Icons.Filled.Leaderboard, "Sin datos", "Aún no hay candidaturas registradas.") }
                }
                itemsIndexed(estado.resultados, key = { _, r -> r.candidataId }) { indice, resultado ->
                    TarjetaResultado(
                        posicion = indice + 1,
                        resultado = resultado,
                        onClick = { onAbrir(resultado.candidataId) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun Indicador(titulo: String, valor: String, icono: ImageVector, modifier: Modifier = Modifier) {
    // Si el valor es entero, se anima como contador
    val entero = valor.toIntOrNull()
    val animado by animateIntAsState(entero ?: 0, tween(800), label = "contador$titulo")
    ElevatedCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                if (entero != null) animado.toString() else valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(titulo, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TarjetaResultado(
    posicion: Int,
    resultado: ResultadoConsolidado,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val objetivo = ((resultado.promedio ?: 0.0) / 100.0).toFloat()
    // La barra "crece" desde 0 al aparecer
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(objetivo) { progreso.animateTo(objetivo, tween(900)) }

    val colorMedalla = when (posicion) {
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFBF8A5A)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = colorMedalla, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$posicion", fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(resultado.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    resultado.area,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LinearProgressIndicator(
                    progress = { progreso.value },
                    color = colorPuntaje(resultado.promedio ?: 0.0),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    if (resultado.totalEvaluaciones == 0) "Sin evaluaciones"
                    else "${formatearPuntaje(resultado.promedio)} pts · ${resultado.totalEvaluaciones} evaluación(es)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.width(8.dp))
            EstadoChip(resultado.estado)
        }
    }
}
