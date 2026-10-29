package cl.duoc.escuelacarabineros.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import cl.duoc.escuelacarabineros.R
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.escuelacarabineros.ui.components.BotonPrincipal
import cl.duoc.escuelacarabineros.ui.components.CampoValidado
import cl.duoc.escuelacarabineros.ui.components.MensajeError
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsState()

    // Navega cuando el ViewModel indica que el login fue exitoso
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) onLoginExitoso()
    }

    // Animación de entrada del logo y el formulario
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            // Escudo institucional (entra con animación de rebote)
            Image(
                painter = painterResource(R.drawable.logo_carabineros),
                contentDescription = "Escudo de Carabineros de Chile",
                modifier = Modifier.size(140.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Escuela de Carabineros", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Evaluación en terreno para docentes e instructores",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it / 3 } + fadeIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    etiqueta = "Contraseña",
                    error = estado.passwordError,
                    icono = Icons.Filled.Lock,
                    esPassword = true,
                    tipoTeclado = KeyboardType.Password
                )

                MensajeError(estado.errorGeneral, Modifier.padding(vertical = 8.dp))
                Spacer(Modifier.height(8.dp))

                BotonPrincipal(
                    texto = "Iniciar sesión",
                    onClick = viewModel::login,
                    cargando = estado.cargando
                )
                Spacer(Modifier.height(8.dp))
                // Cuentas ficticias para la demostración
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = viewModel::usarCuentaDemo) {
                        Text("Demo docente")
                    }
                    OutlinedButton(onClick = viewModel::usarCuentaAdmin) {
                        Text("Demo admin")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "¿No tienes cuenta? Solicítala al administrador de la Escuela.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
