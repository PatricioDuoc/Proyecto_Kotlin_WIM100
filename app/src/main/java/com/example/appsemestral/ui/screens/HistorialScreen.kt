package com.example.appsemestral.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.RegistroHistorial
import com.example.appsemestral.ui.components.EstadoVacio
import com.example.appsemestral.util.formatearFecha
import com.example.appsemestral.ui.viewmodel.AppViewModelProvider
import com.example.appsemestral.ui.viewmodel.HistorialViewModel

/** Trazabilidad global: quién hizo qué y cuándo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Historial de acciones") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::onBusqueda,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Filtrar por acción, detalle o persona") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onBusqueda("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                estado.registros.isEmpty() -> EstadoVacio(Icons.Filled.History, "Sin registros", "Las acciones realizadas aparecerán aquí.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.registros, key = { it.id }) { registro ->
                        ItemHistorial(registro, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

/** Ítem reutilizable (también se usa en el detalle de cada candidatura). */
@Composable
fun ItemHistorial(registro: RegistroHistorial, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    iconoAccion(registro.accion),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(registro.accion, style = MaterialTheme.typography.titleSmall)
            Text(registro.detalle, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${registro.usuario} · ${formatearFecha(registro.fecha)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun iconoAccion(accion: String): ImageVector = when (accion) {
    Acciones.INICIO_SESION -> Icons.AutoMirrored.Filled.Login
    Acciones.CIERRE_SESION -> Icons.AutoMirrored.Filled.Logout
    Acciones.REGISTRO -> Icons.Filled.PersonAdd
    Acciones.EVALUACION -> Icons.AutoMirrored.Filled.Assignment
    Acciones.INTENTO_DUPLICADO -> Icons.Filled.Block
    Acciones.RESOLUCION -> Icons.Filled.Gavel
    Acciones.CONSULTA_CV -> Icons.Filled.PictureAsPdf
    Acciones.CONSULTA_PERFIL -> Icons.Filled.Link
    Acciones.COMPARTIR -> Icons.Filled.Share
    else -> Icons.Filled.History
}
