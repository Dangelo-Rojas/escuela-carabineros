package cl.duoc.escuelacarabineros.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import cl.duoc.escuelacarabineros.model.RolDocente
import cl.duoc.escuelacarabineros.ui.components.EstadoVacio
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.CuentasViewModel

/** Pantalla del Administrador: lista de cuentas de docentes e instructores. */
@Composable
fun CuentasScreen(
    onCrearCuenta: () -> Unit,
    onEditarCuenta: (Long) -> Unit,
    viewModel: CuentasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<DocenteEntity?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.semantics { contentDescription = "Nueva cuenta" },
                onClick = onCrearCuenta,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva cuenta") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                placeholder = { Text("Buscar por nombre, correo o rol") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (estado.busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onBusquedaChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(Modifier.fillMaxSize()) {
                when {
                    estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                    estado.cuentas.isEmpty() -> EstadoVacio(
                        icono = Icons.Filled.Person,
                        titulo = "Sin resultados",
                        detalle = "No hay cuentas que coincidan con la búsqueda.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estado.cuentas, key = { it.id }) { cuenta ->
                            TarjetaCuenta(
                                cuenta = cuenta,
                                esYo = cuenta.id == viewModel.miId,
                                onActivo = { viewModel.cambiarActivo(cuenta, it) },
                                onEditar = { onEditarCuenta(cuenta.id) },
                                onEliminar = { porEliminar = cuenta },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { cuenta ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar cuenta?") },
            text = {
                Text(
                    "Se eliminará la cuenta de ${cuenta.nombre} (${cuenta.correo}). " +
                        "Si solo quieres quitarle el acceso, usa el interruptor \"Acceso\"."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(cuenta)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaCuenta(
    cuenta: DocenteEntity,
    esYo: Boolean,
    onActivo: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esAdmin = cuenta.rol == RolDocente.ADMINISTRADOR.etiqueta
    // El avatar cambia de color con animación al activar/desactivar la cuenta
    val colorAvatar by animateColorAsState(
        if (cuenta.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "colorAvatar"
    )

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .background(colorAvatar, CircleShape)
                ) {
                    Text(
                        cuenta.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .alpha(if (cuenta.activo) 1f else 0.6f)
                ) {
                    Text(
                        cuenta.nombre + if (esYo) " (tú)" else "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        cuenta.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.padding(top = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = onEditar,
                    label = { Text(cuenta.rol) },
                    colors = if (esAdmin) AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) else AssistChipDefaults.assistChipColors()
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (cuenta.activo) "Acceso" else "Sin acceso",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = cuenta.activo,
                    onCheckedChange = onActivo,
                    enabled = !esYo // el admin no puede quitarse el acceso a sí mismo
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Row {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Editar / cambiar rol")
                }
                Spacer(Modifier.weight(1f))
                if (!esYo) {
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar cuenta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
