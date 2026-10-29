package cl.duoc.escuelacarabineros.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto reutilizable con retroalimentación visual:
 * - ícono de advertencia y borde rojo cuando hay error,
 * - ícono de check verde cuando el campo es válido,
 * - mensaje de error animado bajo el campo.
 * El error lo calcula el ViewModel; este componente solo lo muestra.
 */
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    placeholder: String? = null,
    ayuda: String? = null,
    esPassword: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true,
    minLineas: Int = 1,
    habilitado: Boolean = true,
    soloLectura: Boolean = false
) {
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    val esValido = valor.isNotBlank() && error == null

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else null,
        leadingIcon = if (icono != null) {
            { Icon(icono, contentDescription = null) }
        } else null,
        trailingIcon = {
            Row {
                if (esPassword) {
                    TextButton(onClick = { mostrarPassword = !mostrarPassword }) {
                        Text(if (mostrarPassword) "Ocultar" else "Ver")
                    }
                }
                when {
                    error != null -> Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    esValido -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Campo válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        isError = error != null,
        supportingText = {
            AnimatedVisibility(
                visible = error != null || ayuda != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = error ?: ayuda.orEmpty(),
                    color = if (error != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        visualTransformation = if (esPassword && !mostrarPassword)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = unaLinea,
        minLines = minLineas,
        enabled = habilitado,
        readOnly = soloLectura,
        modifier = modifier.fillMaxWidth()
    )
}
