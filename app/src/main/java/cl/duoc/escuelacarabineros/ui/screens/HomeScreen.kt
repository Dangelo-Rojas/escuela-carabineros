package cl.duoc.escuelacarabineros.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.data.local.entity.RubricaConCriterios
import cl.duoc.escuelacarabineros.ui.components.EstadoVacio
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onCrearRubrica: () -> Unit,
    onEvaluar: (Long) -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    var porEliminar by remember { mutableStateOf<RubricaConCriterios?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.semantics { contentDescription = "Nueva rúbrica" },
                onClick = onCrearRubrica,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva rúbrica") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                estado.rubricas.isEmpty() -> EstadoVacio(
                    icono = Icons.Filled.Info,
                    titulo = "Aún no tienes rúbricas",
                    detalle = "Crea tu primera rúbrica con el botón \"Nueva rúbrica\".",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(estado.rubricas, key = { it.rubrica.id }) { item ->
                        TarjetaRubrica(
                            item = item,
                            onEvaluar = { onEvaluar(item.rubrica.id) },
                            onEditar = { onEditar(item.rubrica.id) },
                            onEliminar = { porEliminar = item },
                            modifier = Modifier.animateItem() // anima altas/bajas en la lista
                        )
                    }
                }
            }
        }
    }

    porEliminar?.let { rubrica ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar rúbrica?") },
            text = { Text("Se eliminará \"${rubrica.rubrica.nombre}\" y sus evaluaciones asociadas.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(rubrica)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaRubrica(
    item: RubricaConCriterios,
    onEvaluar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(item.rubrica.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                item.rubrica.asignatura,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (item.rubrica.descripcion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    item.rubrica.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = {},
                label = { Text("${item.criterios.size} criterios") }
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar rúbrica",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar rúbrica")
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onEvaluar) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Evaluar")
                }
            }
        }
    }
}
