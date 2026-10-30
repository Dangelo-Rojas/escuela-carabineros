package cl.duoc.escuelacarabineros.ui.components

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.escuelacarabineros.R
import cl.duoc.escuelacarabineros.navigation.Rutas

/** Barras compartidas por las secciones principales; los formularios conservan su propio Scaffold. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionPrincipal(
    rutaActual: String?,
    nombreUsuario: String,
    esAdministrador: Boolean,
    modoTema: String,
    onCambiarTema: (String) -> Unit,
    onSeccion: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val mostrarBarras = rutaActual in listOf(Rutas.HOME, Rutas.HISTORIAL, Rutas.CUENTAS)
    var mostrarMenu by rememberSaveable { mutableStateOf(false) }
    var mostrarApariencia by rememberSaveable { mutableStateOf(false) }
    var mostrarAcercaDe by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Esfocar", maxLines = 1)
                            Text(
                                "Hola, $nombreUsuario",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        Box {
                            IconButton(onClick = { mostrarMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(
                                expanded = mostrarMenu,
                                onDismissRequest = { mostrarMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Apariencia") },
                                    onClick = {
                                        mostrarMenu = false
                                        mostrarApariencia = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Acerca de Esfocar") },
                                    onClick = {
                                        mostrarMenu = false
                                        mostrarAcercaDe = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cerrar sesión") },
                                    onClick = {
                                        mostrarMenu = false
                                        onCerrarSesion()
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (mostrarBarras) {
                val secciones = buildList {
                    add(Triple(Rutas.HOME, "Rúbricas", Icons.AutoMirrored.Filled.List))
                    add(Triple(Rutas.HISTORIAL, "Historial", ImageVector.vectorResource(R.drawable.ic_historial)))
                    if (esAdministrador) {
                        add(Triple(Rutas.CUENTAS, "Cuentas", Icons.Filled.AccountCircle))
                    }
                }
                NavigationBar {
                    secciones.forEach { (ruta, titulo, icono) ->
                        NavigationBarItem(
                            selected = rutaActual == ruta,
                            onClick = { onSeccion(ruta) },
                            icon = { Icon(icono, contentDescription = null) },
                            label = { Text(titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        content(Modifier.padding(padding).consumeWindowInsets(padding))
    }

    if (mostrarBarras && mostrarApariencia) {
        AlertDialog(
            onDismissRequest = { mostrarApariencia = false },
            title = { Text("Apariencia") },
            text = {
                Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                    listOf("sistema" to "Sistema", "claro" to "Claro", "oscuro" to "Oscuro")
                        .forEach { (valor, etiqueta) ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .selectable(
                                        selected = modoTema == valor,
                                        role = Role.RadioButton,
                                        onClick = { onCambiarTema(valor) }
                                    )
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(selected = modoTema == valor, onClick = null)
                                Text(etiqueta)
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarApariencia = false }) { Text("Listo") }
            }
        )
    }

    if (mostrarBarras && mostrarAcercaDe) {
        AcercaDeEsfocar(onCerrar = { mostrarAcercaDe = false })
    }
}

@Composable
private fun AcercaDeEsfocar(onCerrar: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        info.versionName ?: "No disponible"
    }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Acerca de Esfocar") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Esfocar", style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.app_name))
                Text("Versión $version")
                Text(stringResource(R.string.acerca_descripcion))
                Text("Integrantes", style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.acerca_integrantes))
                Text(stringResource(R.string.acerca_academico))
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}
