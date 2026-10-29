package cl.duoc.escuelacarabineros.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Campo de selección (lista desplegable) con la misma retroalimentación
 * visual que CampoValidado: ícono y mensaje de error si falta elegir.
 */
@Composable
fun SelectorOpciones(
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    placeholder: String = "Toca para elegir"
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier) {
        CampoValidado(
            valor = valor,
            onValorChange = {},
            etiqueta = etiqueta,
            error = error,
            icono = icono,
            placeholder = placeholder,
            soloLectura = true
        )
        // Capa transparente encima del campo: al tocarla se abre la lista
        Box(
            Modifier
                .matchParentSize()
                .clickable { abierto = true }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        abierto = false
                    },
                    leadingIcon = if (opcion == valor) {
                        { Icon(Icons.Filled.Check, contentDescription = "Seleccionada") }
                    } else null
                )
            }
        }
    }
}
