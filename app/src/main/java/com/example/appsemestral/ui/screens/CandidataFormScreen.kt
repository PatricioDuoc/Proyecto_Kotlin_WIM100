package com.example.appsemestral.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.nativo.ArchivoHelper
import com.example.appsemestral.ui.components.BannerInfo
import com.example.appsemestral.ui.components.CampoValidado
import com.example.appsemestral.ui.components.MensajeError
import com.example.appsemestral.ui.components.TituloSeccion
import com.example.appsemestral.ui.components.sacudir
import com.example.appsemestral.util.formatearTamano
import com.example.appsemestral.validation.CandidataValidator
import com.example.appsemestral.ui.viewmodel.AppViewModelProvider
import com.example.appsemestral.ui.viewmodel.CandidataFormViewModel

/** Registro de candidatura con validación por campo, íconos y mensajes de error. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandidataFormScreen(
    sesion: Sesion,
    onVolver: () -> Unit,
    mostrarMensaje: (String) -> Unit,
    viewModel: CandidataFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // RECURSO NATIVO: selector de documentos del sistema (solo PDF)
    val selectorCv = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            ArchivoHelper.persistirPermiso(context, uri)
            val info = ArchivoHelper.leerInfo(context, uri)
            viewModel.onCv(uri.toString(), info.nombre, info.tamano)
        }
    }

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            mostrarMensaje("Candidatura registrada correctamente")
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
                title = { Text("Nueva candidatura") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            BannerInfo(
                "Usa solo datos ficticios: nombres inventados, correos @ejemplo.cl y un CV de prueba.",
                icono = Icons.Filled.VerifiedUser
            )

            TituloSeccion("Datos de contacto")
            CampoValidado(
                valor = estado.nombre.valor,
                onValorChange = viewModel::onNombre,
                etiqueta = "Nombre completo",
                icono = Icons.Filled.Person,
                error = estado.nombre.error,
                capitalization = KeyboardCapitalization.Words
            )
            CampoValidado(
                valor = estado.correo.valor,
                onValorChange = viewModel::onCorreo,
                etiqueta = "Correo",
                icono = Icons.Filled.Email,
                error = estado.correo.error,
                placeholder = "nombre.apellido@ejemplo.cl",
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None
            )
            CampoValidado(
                valor = estado.telefono.valor,
                onValorChange = viewModel::onTelefono,
                etiqueta = "Teléfono",
                icono = Icons.Filled.Phone,
                error = estado.telefono.error,
                placeholder = "+56 9 1234 5678",
                keyboardType = KeyboardType.Phone
            )

            TituloSeccion("Perfil profesional")
            SelectorArea(
                seleccionada = estado.area,
                error = estado.errorArea,
                onSeleccionar = viewModel::onArea
            )
            CampoValidado(
                valor = estado.anios.valor,
                onValorChange = viewModel::onAnios,
                etiqueta = "Años de experiencia",
                icono = Icons.Filled.Work,
                error = estado.anios.error,
                keyboardType = KeyboardType.Number,
                maxLength = 2
            )
            CampoValidado(
                valor = estado.resumen.valor,
                onValorChange = viewModel::onResumen,
                etiqueta = "Resumen de experiencia",
                icono = Icons.Filled.Description,
                error = estado.resumen.error,
                singleLine = false,
                minLines = 3,
                maxLength = CandidataValidator.RESUMEN_MAX,
                imeAction = ImeAction.Default
            )
            CampoValidado(
                valor = estado.linkedin.valor,
                onValorChange = viewModel::onLinkedin,
                etiqueta = "Perfil profesional (enlace ficticio)",
                icono = Icons.Filled.Link,
                error = estado.linkedin.error,
                placeholder = "https://www.linkedin.com/in/usuario-demo",
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            )

            TituloSeccion("Antecedentes")
            SelectorCv(
                nombre = estado.cvNombre,
                tamano = estado.cvTamano,
                error = estado.errorCv,
                onSeleccionar = { selectorCv.launch(arrayOf("application/pdf")) }
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.guardar(sesion.nombre) },
                enabled = !estado.guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .sacudir(estado.intentosFallidos)
            ) {
                AnimatedContent(targetState = estado.guardando, label = "botonGuardar") { guardando ->
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar candidatura")
                        }
                    }
                }
            }
            MensajeError(
                error = if (estado.intentosFallidos > 0 && estado.hayErrores) "Revisa los campos marcados en rojo" else null,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SelectorArea(seleccionada: String?, error: String?, onSeleccionar: (String) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Category,
                contentDescription = null,
                tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text("Área de especialidad", style = MaterialTheme.typography.bodyLarge)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Candidata.AREAS.forEach { area ->
                val activa = area == seleccionada
                FilterChip(
                    selected = activa,
                    onClick = { onSeleccionar(area) },
                    label = { Text(area) },
                    leadingIcon = if (activa) {
                        { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }
        MensajeError(error)
    }
}

@Composable
private fun SelectorCv(nombre: String?, tamano: Long?, error: String?, onSeleccionar: () -> Unit) {
    val colorError = MaterialTheme.colorScheme.error
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = if (error != null) BorderStroke(1.dp, colorError) else CardDefaults.outlinedCardBorder()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.PictureAsPdf,
                contentDescription = null,
                tint = if (error != null) colorError else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    nombre ?: "CV de prueba (PDF)",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (nombre != null) formatearTamano(tamano) else "Archivo PDF ficticio, máximo 5 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onSeleccionar) {
                Icon(Icons.Filled.AttachFile, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (nombre == null) "Adjuntar" else "Cambiar")
            }
        }
    }
    MensajeError(error)
}
