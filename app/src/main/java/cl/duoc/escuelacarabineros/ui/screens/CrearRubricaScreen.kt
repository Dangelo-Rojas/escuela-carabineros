package cl.duoc.escuelacarabineros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.rotate
import cl.duoc.escuelacarabineros.model.NivelLogro
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.ui.components.BotonPrincipal
import cl.duoc.escuelacarabineros.ui.components.CampoValidado
import cl.duoc.escuelacarabineros.ui.components.MensajeError
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.CrearRubricaViewModel
import cl.duoc.escuelacarabineros.viewmodel.CriterioForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRubricaScreen(
    onVolver: () -> Unit,
    viewModel: CrearRubricaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar rúbrica" else "Nueva rúbrica") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            Text("Datos generales", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            CampoValidado(
                valor = estado.nombre,
                onValorChange = viewModel::onNombreChange,
                etiqueta = "Nombre de la rúbrica",
                placeholder = "Ej: Control vehicular preventivo",
                error = estado.nombreError,
                icono = Icons.Filled.Edit
            )
            CampoValidado(
                valor = estado.asignatura,
                onValorChange = viewModel::onAsignaturaChange,
                etiqueta = "Asignatura",
                placeholder = "Ej: Técnicas y Tácticas Policiales",
                error = estado.asignaturaError,
                icono = Icons.Filled.Info
            )
            CampoValidado(
                valor = estado.descripcion,
                onValorChange = viewModel::onDescripcionChange,
                etiqueta = "Descripción (opcional)",
                error = estado.descripcionError,
                ayuda = "${estado.descripcion.length}/300",
                unaLinea = false,
                minLineas = 2
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Criterios de evaluación", style = MaterialTheme.typography.titleMedium)
            Text(
                "Cada criterio tiene una ponderación. La suma debe ser 100 %.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            IndicadorSuma(suma = estado.sumaPonderaciones)
            Spacer(Modifier.height(8.dp))

            estado.criterios.forEachIndexed { indice, criterio ->
                key(criterio.idLocal) {
                    TarjetaCriterio(
                        numero = indice + 1,
                        criterio = criterio,
                        onDescripcion = { viewModel.onCriterioDescripcionChange(criterio.idLocal, it) },
                        onPonderacion = { viewModel.onCriterioPonderacionChange(criterio.idLocal, it) },
                        onDescNivel = { nivel, texto -> viewModel.onDescNivelChange(criterio.idLocal, nivel, texto) },
                        onEliminar = { viewModel.eliminarCriterio(criterio.idLocal) }
                    )
                }
            }

            MensajeError(estado.criteriosError, Modifier.padding(vertical = 4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = viewModel::agregarCriterio) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Criterio")
                }
                TextButton(onClick = viewModel::repartirPonderacionEquitativa) {
                    Text("Repartir 100 % parejo")
                }
            }

            Spacer(Modifier.height(16.dp))
            BotonPrincipal(
                texto = if (estado.esEdicion) "Guardar cambios" else "Guardar rúbrica",
                onClick = viewModel::guardar,
                cargando = estado.guardando,
                icono = Icons.Filled.Check
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Barra que se llena y cambia de color según la suma de ponderaciones. */
@Composable
private fun IndicadorSuma(suma: Int) {
    val progreso by animateFloatAsState((suma.coerceAtMost(100)) / 100f, label = "progresoSuma")
    val color by animateColorAsState(
        when {
            suma == 100 -> MaterialTheme.colorScheme.primary
            suma > 100 -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondary
        },
        label = "colorSuma"
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Suma de ponderaciones", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("$suma %", style = MaterialTheme.typography.titleMedium, color = color)
        }
        LinearProgressIndicator(
            progress = { progreso },
            color = color,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun TarjetaCriterio(
    numero: Int,
    criterio: CriterioForm,
    onDescripcion: (String) -> Unit,
    onPonderacion: (String) -> Unit,
    onDescNivel: (NivelLogro, String) -> Unit,
    onEliminar: () -> Unit
) {
    // Si ya tiene descripciones (al editar), la sección parte abierta
    var mostrarNiveles by rememberSaveable { mutableStateOf(criterio.descNiveles.values.any { it.isNotBlank() }) }
    val rotacion by animateFloatAsState(if (mostrarNiveles) 180f else 0f, label = "flechaNiveles")
    // Cada criterio nuevo aparece con una animación de entrada
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = visible, enter = fadeIn() + expandVertically()) {
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Criterio $numero", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Filled.Close, contentDescription = "Quitar criterio $numero")
                    }
                }
                CampoValidado(
                    valor = criterio.descripcion,
                    onValorChange = onDescripcion,
                    etiqueta = "Descripción del criterio",
                    error = criterio.descripcionError,
                    unaLinea = false
                )
                CampoValidado(
                    valor = criterio.ponderacion,
                    onValorChange = onPonderacion,
                    etiqueta = "Ponderación (%)",
                    error = criterio.ponderacionError,
                    tipoTeclado = KeyboardType.Number
                )

                // ---------- Descripción de cada nivel de logro (desplegable) ----------
                TextButton(onClick = { mostrarNiveles = !mostrarNiveles }) {
                    Text(if (mostrarNiveles) "Ocultar descripción de niveles" else "Describir niveles de logro (opcional)")
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotacion)
                    )
                }
                AnimatedVisibility(visible = mostrarNiveles) {
                    Column {
                        Text(
                            "Explica qué debe demostrar el alumno en cada nivel. Esto sirve como retroalimentación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        NivelLogro.entries.forEach { nivel ->
                            val texto = criterio.descNiveles[nivel].orEmpty()
                            CampoValidado(
                                valor = texto,
                                onValorChange = { onDescNivel(nivel, it) },
                                etiqueta = "${nivel.etiqueta} (${nivel.porcentaje} %)",
                                error = criterio.descNivelesError[nivel],
                                ayuda = "${texto.length}/${CrearRubricaViewModel.MAX_DESC_NIVEL}",
                                unaLinea = false
                            )
                        }
                    }
                }
            }
        }
    }
}
