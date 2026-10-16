package com.example.appsemestral.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Campo de texto reutilizable con:
 * - ícono descriptivo a la izquierda (cambia a color de error si el campo es inválido),
 * - ícono de error / check a la derecha,
 * - mensaje de error animado bajo el campo y contador de caracteres opcional.
 * El composable NO valida: solo muestra el `error` que le entrega el ViewModel.
 */
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    ayuda: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLength: Int? = null,
    mostrarValido: Boolean = true
) {
    val hayError = error != null
    val colorIcono = if (hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo -> if (maxLength == null || nuevo.length <= maxLength) onValorChange(nuevo) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = { Text(etiqueta) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else null,
        leadingIcon = { Icon(icono, contentDescription = null, tint = colorIcono) },
        trailingIcon = when {
            hayError -> {
                { Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error) }
            }
            mostrarValido && valor.isNotBlank() -> {
                { Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = MaterialTheme.colorScheme.secondary) }
            }
            else -> null
        },
        isError = hayError,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        supportingText = if (hayError || ayuda != null || maxLength != null) {
            {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = error ?: ayuda.orEmpty(),
                        color = if (hayError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (maxLength != null) Text("${valor.length}/$maxLength")
                }
            }
        } else null
    )
}

/** Mensaje de error con ícono, animado, para controles que no son campos de texto. */
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    // Recuerda el último mensaje para que no desaparezca el texto durante la animación de salida
    var ultimo by remember { mutableStateOf("") }
    if (error != null) ultimo = error
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(ultimo, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
