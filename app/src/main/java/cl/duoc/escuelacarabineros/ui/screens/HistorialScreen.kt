package cl.duoc.escuelacarabineros.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionConRubrica
import cl.duoc.escuelacarabineros.model.CalculadoraNota
import cl.duoc.escuelacarabineros.ui.components.EstadoVacio
import cl.duoc.escuelacarabineros.ui.theme.NotaAprobada
import cl.duoc.escuelacarabineros.ui.theme.NotaReprobada
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.HistorialViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    val generandoPdfId by viewModel.generandoPdfId.collectAsState()
    val pdfListo by viewModel.pdfListo.collectAsState()
    val error by viewModel.error.collectAsState()
    var porEliminar by remember { mutableStateOf<EvaluacionConRubrica?>(null) }
    val context = LocalContext.current

    // Cuando el ViewModel termina el PDF, se abre el menú "Compartir" de Android
    LaunchedEffect(pdfListo) {
        pdfListo?.let { archivo ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Resultado de evaluación")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Compartir evaluación"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No hay apps para compartir el PDF", Toast.LENGTH_SHORT).show()
            }
            viewModel.pdfCompartido()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.errorMostrado()
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                placeholder = { Text("Buscar por alumno, rúbrica o sede") },
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

                    estado.evaluaciones.isEmpty() -> EstadoVacio(
                        icono = Icons.Filled.Search,
                        titulo = if (estado.busqueda.isBlank()) "Sin evaluaciones aún" else "Sin resultados",
                        detalle = if (estado.busqueda.isBlank())
                            "Las evaluaciones que guardes aparecerán aquí."
                        else "No hay evaluaciones que coincidan con \"${estado.busqueda}\".",
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estado.evaluaciones, key = { it.evaluacion.id }) { item ->
                            TarjetaEvaluacion(
                                item = item,
                                generandoPdf = generandoPdfId == item.evaluacion.id,
                                onCompartir = { viewModel.generarPdf(item) },
                                onEliminar = { porEliminar = item },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar evaluación?") },
            text = { Text("Se eliminará la evaluación de ${item.evaluacion.evaluado}.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(item)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

/** Tarjeta que se expande al tocarla para mostrar el detalle (animada). */
@Composable
private fun TarjetaEvaluacion(
    item: EvaluacionConRubrica,
    generandoPdf: Boolean,
    onCompartir: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ev = item.evaluacion
    var expandida by rememberSaveable { mutableStateOf(false) }
    val rotacion by animateFloatAsState(if (expandida) 180f else 0f, label = "flecha")
    val colorNota: Color = if (CalculadoraNota.esAprobada(ev.notaCentesimas)) NotaAprobada else NotaReprobada
    val fecha = remember(ev.fecha) {
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(ev.fecha))
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandida = !expandida }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(colorNota.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        CalculadoraNota.formatear(ev.notaCentesimas),
                        style = MaterialTheme.typography.titleLarge,
                        color = colorNota
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(ev.evaluado, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${ev.tipo} · ${item.nombreRubrica}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (ev.sede.isNotBlank()) "$fecha · ${ev.sede}" else fecha,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expandida) "Ocultar detalle" else "Ver detalle",
                    modifier = Modifier.rotate(rotacion)
                )
            }

            AnimatedVisibility(visible = expandida) {
                Column {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Logro: ${ev.porcentajeLogro} %", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    ev.detalle.lines().forEach { linea ->
                        val partes = linea.split("|")
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text(partes.getOrElse(0) { "" }, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                partes.getOrElse(1) { "" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (ev.comentario.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Comentario", style = MaterialTheme.typography.titleMedium)
                        Text(ev.comentario, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (ev.fotoPath != null) {
                        val bitmap = remember(ev.fotoPath) { BitmapFactory.decodeFile(ev.fotoPath) }
                        if (bitmap != null) {
                            Spacer(Modifier.height(8.dp))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto de evidencia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                    if (ev.latitud != null && ev.longitud != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("%.5f, %.5f".format(ev.latitud, ev.longitud), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onCompartir, enabled = !generandoPdf) {
                            if (generandoPdf) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Share, contentDescription = null)
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir PDF")
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onEliminar) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(4.dp))
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
