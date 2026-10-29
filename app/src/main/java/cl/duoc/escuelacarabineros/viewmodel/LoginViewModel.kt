    package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.repository.ResultadoAuth
import cl.duoc.escuelacarabineros.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val correoError: String? = null,
    val passwordError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false
) {
    val formularioValido: Boolean
        get() = correo.isNotBlank() && password.isNotBlank() &&
            correoError == null && passwordError == null
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) = _uiState.update {
        it.copy(correo = valor, correoError = Validadores.correo(valor), errorGeneral = null)
    }

    fun onPasswordChange(valor: String) = _uiState.update {
        it.copy(password = valor, passwordError = Validadores.passwordLogin(valor), errorGeneral = null)
    }

    fun login() {
        val estado = _uiState.value
        val correoError = Validadores.correo(estado.correo)
        val passwordError = Validadores.passwordLogin(estado.password)
        _uiState.update { it.copy(correoError = correoError, passwordError = passwordError) }
        if (correoError != null || passwordError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null) }
            when (val resultado = authRepository.login(estado.correo, estado.password)) {
                is ResultadoAuth.Exito -> _uiState.update { it.copy(cargando = false, loginExitoso = true) }
                is ResultadoAuth.Error -> _uiState.update {
                    it.copy(cargando = false, errorGeneral = resultado.mensaje)
                }
            }
        }
    }

    /** Rellena la cuenta de prueba de docente (útil para la presentación). */
    fun usarCuentaDemo() {
        onCorreoChange(AuthRepository.CORREO_DEMO)
        onPasswordChange(AuthRepository.PASSWORD_DEMO)
    }

    /** Rellena la cuenta de prueba de administrador. */
    fun usarCuentaAdmin() {
        onCorreoChange(AuthRepository.CORREO_ADMIN)
        onPasswordChange(AuthRepository.PASSWORD_ADMIN)
    }
}
