package cl.duoc.escuelacarabineros.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.animation.AnimatedContent
import cl.duoc.escuelacarabineros.model.Sedes
import cl.duoc.escuelacarabineros.ui.components.SelectorOpciones
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.data.local.entity.CriterioEntity
import cl.duoc.escuelacarabineros.model.CalculadoraNota
import cl.duoc.escuelacarabineros.model.NivelLogro
import cl.duoc.escuelacarabineros.model.TipoEvaluacion
import cl.duoc.escuelacarabineros.ui.components.BotonPrincipal
import cl.duoc.escuelacarabineros.ui.components.CampoValidado
import cl.duoc.escuelacarabineros.ui.components.EstadoVacio
import cl.duoc.escuelacarabineros.ui.components.MensajeError
import cl.duoc.escuelacarabineros.ui.theme.NotaAprobada
import cl.duoc.escuelacarabineros.ui.theme.NotaReprobada
import cl.duoc.escuelacarabineros.utils.UbicacionHelper
import cl.duoc.escuelacarabineros.utils.Vibracion
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.EvaluarUiState
import cl.duoc.escuelacarabineros.viewmodel.EvaluarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluarScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: EvaluarViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // ---------- RECURSO NATIVO 1: CÁMARA ----------
    val tomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) viewModel.onFotoTomada(bitmap) }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) tomarFoto.launch(null) else viewModel.onPermisoDenegado("cámara")
    }

    val abrirCamara: () -> Unit = {
        val yaConcedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (yaConcedido) tomarFoto.launch(null) else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    // ---------- RECURSO NATIVO 2: GPS / UBICACIÓN ----------
    val buscarUbicacion: () -> Unit = {
        if (!UbicacionHelper.gpsActivado(context)) {
            viewModel.onGpsDesactivado()
        } else {
            viewModel.onBuscandoUbicacion()
            scope.launch {
                val ubicacion = UbicacionHelper.obtenerUbicacion(context)
                viewModel.onUbicacionObtenida(ubicacion?.latitude, ubicacion?.longitude)
            }
        }
    }

    val pedirPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        if (resultados.values.any { it }) buscarUbicacion() else viewModel.onPermisoDenegado("ubicación")
    }

    val registrarUbicacion: () -> Unit = {
        val yaConcedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (yaConcedido) {
            buscarUbicacion()
        } else {
            pedirPermisoUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Mensajes (permisos denegados, GPS apagado, etc.)
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    // RECURSO NATIVO 3 (extra): vibración al guardar + navegación
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            Vibracion.corta(context)
            onGuardado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluar") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val rubrica = estado.rubrica
        when {
            estado.cargando -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }

            rubrica == null -> EstadoVacio(
                icono = Icons.Filled.Close,
                titulo = "Rúbrica no encontrada",
                detalle = "Vuelve al inicio e intenta nuevamente.",
                modifier = Modifier.padding(padding)
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
            // ---------- Encabezado FIJO: rúbrica + nota en vivo ----------
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(rubrica.rubrica.nombre, style = MaterialTheme.typography.titleLarge)
                Text(
                    rubrica.rubrica.asignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                TarjetaNota(estado)
            }
            HorizontalDivider()

            // ---------- Contenido DESPLAZABLE (scroll) ----------
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // ---------- Datos del evaluado ----------
                Text("¿A quién evalúas?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoEvaluacion.entries.forEach { tipo ->
                        FilterChip(
                            selected = estado.tipo == tipo,
                            onClick = { viewModel.onTipoChange(tipo) },
                            label = { Text(tipo.etiqueta) },
                            leadingIcon = if (estado.tipo == tipo) {
                                { Icon(Icons.Filled.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
                CampoValidado(
                    valor = estado.evaluado,
                    onValorChange = viewModel::onEvaluadoChange,
                    etiqueta = if (estado.tipo == TipoEvaluacion.INDIVIDUAL) "Nombre del alumno" else "Nombre del grupo",
                    placeholder = if (estado.tipo == TipoEvaluacion.INDIVIDUAL) "Ej: Juan Pérez" else "Ej: Sección 3 - Grupo A",
                    error = estado.evaluadoError,
                    icono = Icons.Filled.Person
                )
                SelectorOpciones(
                    valor = estado.sede,
                    opciones = Sedes.lista,
                    onSeleccion = viewModel::onSedeChange,
                    etiqueta = "Sede",
                    error = estado.sedeError,
                    icono = Icons.Filled.Home
                )

                // ---------- Criterios ----------
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Criterios", style = MaterialTheme.typography.titleMedium)
                rubrica.criterios.forEachIndexed { i, criterio ->
                    TarjetaCriterioEvaluacion(
                        numero = i + 1,
                        criterio = criterio,
                        seleccionado = estado.niveles[criterio.id],
                        mostrarError = estado.nivelesError != null && estado.niveles[criterio.id] == null,
                        onSeleccionar = { nivel -> viewModel.onNivelSeleccionado(criterio.id, nivel) }
                    )
                }
                MensajeError(estado.nivelesError, Modifier.padding(vertical = 4.dp))

                // ---------- Comentario ----------
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                CampoValidado(
                    valor = estado.comentario,
                    onValorChange = viewModel::onComentarioChange,
                    etiqueta = "Comentario del docente (opcional)",
                    error = estado.comentarioError,
                    ayuda = "${estado.comentario.length}/${EvaluarViewModel.MAX_COMENTARIO}",
                    unaLinea = false,
                    minLineas = 3
                )

                // ---------- Evidencias (recursos nativos) ----------
                Text("Evidencias", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = abrirCamara, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (estado.fotoPath == null) "Tomar foto" else "Cambiar foto")
                    }
                    OutlinedButton(
                        onClick = registrarUbicacion,
                        enabled = !estado.obteniendoUbicacion,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (estado.obteniendoUbicacion) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.LocationOn, contentDescription = null)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Ubicación")
                    }
                }

                VistaPreviaFoto(estado.fotoPath, onQuitar = viewModel::quitarFoto)

                AnimatedVisibility(visible = estado.latitud != null) {
                    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Lat: %.5f   Lon: %.5f".format(estado.latitud ?: 0.0, estado.longitud ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ---------- Botón FIJO abajo: siempre visible ----------
            Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    // Indica que hay más contenido abajo mientras no se llegue al final
                    AnimatedVisibility(visible = scroll.canScrollForward) {
                        Text(
                            "Desliza hacia arriba para ver más ↓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    BotonPrincipal(
                        texto = "Guardar evaluación",
                        onClick = viewModel::guardar,
                        cargando = estado.guardando,
                        icono = Icons.Filled.Check
                    )
                }
            }
            }
        }
    }
}

/** Nota calculada en vivo: el número "cuenta" hasta el valor nuevo y cambia de color. */
@Composable
private fun TarjetaNota(estado: EvaluarUiState) {
    val notaAnimada by animateIntAsState(estado.notaCentesimas, tween(500), label = "nota")
    val color by animateColorAsState(
        if (CalculadoraNota.esAprobada(estado.notaCentesimas)) NotaAprobada else NotaReprobada,
        label = "colorNota"
    )
    val progreso by animateFloatAsState(
        if (estado.totalCriterios == 0) 0f else estado.criteriosEvaluados / estado.totalCriterios.toFloat(),
        label = "progresoCriterios"
    )

    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Nota", style = MaterialTheme.typography.bodyMedium)
                Text(
                    CalculadoraNota.formatear(notaAnimada),
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
                Text(
                    "Logro: ${estado.logroCentesimas / 100} %",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${estado.criteriosEvaluados}/${estado.totalCriterios} criterios",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(progress = { progreso }, modifier = Modifier.width(120.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaCriterioEvaluacion(
    numero: Int,
    criterio: CriterioEntity,
    seleccionado: NivelLogro?,
    mostrarError: Boolean,
    onSeleccionar: (NivelLogro) -> Unit
) {
    val borde by animateColorAsState(
        if (mostrarError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "fondoCriterio"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = borde),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "$numero. ${criterio.descripcion}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Ponderación: ${criterio.ponderacion} %",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NivelLogro.entries.forEach { nivel ->
                    FilterChip(
                        selected = seleccionado == nivel,
                        onClick = { onSeleccionar(nivel) },
                        label = { Text("${nivel.etiqueta} (${nivel.porcentaje} %)") },
                        leadingIcon = if (seleccionado == nivel) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            // Muestra qué significa el nivel elegido (cambia con animación)
            val descripcionNivel = when (seleccionado) {
                NivelLogro.LOGRADO -> criterio.descLogrado
                NivelLogro.ACEPTABLE -> criterio.descAceptable
                NivelLogro.INCIPIENTE -> criterio.descIncipiente
                NivelLogro.NO_LOGRADO -> criterio.descNoLogrado
                null -> ""
            }
            AnimatedContent(targetState = descripcionNivel, label = "descNivel") { texto ->
                if (texto.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Text(
                            texto,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VistaPreviaFoto(ruta: String?, onQuitar: () -> Unit) {
    val bitmap = remember(ruta) { ruta?.let { BitmapFactory.decodeFile(it) } }
    AnimatedVisibility(
        visible = bitmap != null,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(Modifier.padding(top = 8.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de evidencia",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            IconButton(onClick = onQuitar, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Close, contentDescription = "Quitar foto")
            }
        }
    }
}
