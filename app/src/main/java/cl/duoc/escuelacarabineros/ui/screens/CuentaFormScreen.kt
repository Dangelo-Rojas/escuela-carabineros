package cl.duoc.escuelacarabineros.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.model.RolDocente
import cl.duoc.escuelacarabineros.ui.components.BotonPrincipal
import cl.duoc.escuelacarabineros.ui.components.CampoValidado
import cl.duoc.escuelacarabineros.ui.components.MensajeError
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.CuentaFormViewModel

/** Formulario del Administrador para crear o editar una cuenta y asignar su rol. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CuentaFormScreen(
    onVolver: () -> Unit,
    viewModel: CuentaFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) onVolver()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar cuenta" else "Nueva cuenta") },
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                if (estado.esEdicion) "Modifica los datos o el rol de la cuenta"
                else "Crea la cuenta de un docente o instructor de la Escuela",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            CampoValidado(
                valor = estado.nombre,
                onValorChange = viewModel::onNombreChange,
                etiqueta = "Nombre completo",
                error = estado.nombreError,
                icono = Icons.Filled.Person
            )
            CampoValidado(
                valor = estado.correo,
                onValorChange = viewModel::onCorreoChange,
                etiqueta = "Correo",
                placeholder = "nombre@dominio.cl",
                error = estado.correoError,
                icono = Icons.Filled.Email,
                tipoTeclado = KeyboardType.Email
            )
            CampoValidado(
                valor = estado.password,
                onValorChange = viewModel::onPasswordChange,
                etiqueta = if (estado.esEdicion) "Nueva contraseña (opcional)" else "Contraseña",
                error = estado.passwordError,
                ayuda = if (estado.esEdicion) "Déjala vacía para mantener la actual"
                else "Mínimo 6 caracteres, con letras y números",
                icono = Icons.Filled.Lock,
                esPassword = true,
                tipoTeclado = KeyboardType.Password
            )
            if (!estado.esEdicion || estado.password.isNotEmpty()) {
                CampoValidado(
                    valor = estado.confirmacion,
                    onValorChange = viewModel::onConfirmacionChange,
                    etiqueta = "Confirmar contraseña",
                    error = estado.confirmacionError,
                    icono = Icons.Filled.Lock,
                    esPassword = true,
                    tipoTeclado = KeyboardType.Password
                )
            }

            Text("Rol asignado", style = MaterialTheme.typography.titleMedium)
            Text(
                "El Administrador puede gestionar cuentas; Docente e Instructor solo evalúan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RolDocente.entries.forEach { rol ->
                    FilterChip(
                        selected = estado.rol == rol,
                        onClick = { viewModel.onRolChange(rol) },
                        label = { Text(rol.etiqueta) },
                        leadingIcon = if (estado.rol == rol) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            MensajeError(estado.errorGeneral, Modifier.padding(vertical = 8.dp))
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(
                texto = if (estado.esEdicion) "Guardar cambios" else "Crear cuenta",
                onClick = viewModel::guardar,
                cargando = estado.guardando,
                icono = Icons.Filled.Check
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
