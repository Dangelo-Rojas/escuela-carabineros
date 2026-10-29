package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.repository.ResultadoAuth
import cl.duoc.escuelacarabineros.model.RolDocente
import cl.duoc.escuelacarabineros.navigation.Rutas
import cl.duoc.escuelacarabineros.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CuentaFormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val rol: RolDocente = RolDocente.DOCENTE,
    val nombreError: String? = null,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmacionError: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)

/**
 * Formulario del administrador para CREAR una cuenta (sin argumento en la ruta)
 * o EDITARLA (con el id de la cuenta). Al editar, la contraseña es opcional:
 * si se deja vacía se mantiene la actual.
 */
class CuentaFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val cuentaId: Long = savedStateHandle.get<Long>(Rutas.ARG_CUENTA_ID) ?: SIN_ID
    private var original: DocenteEntity? = null

    private val _uiState = MutableStateFlow(CuentaFormUiState(esEdicion = cuentaId != SIN_ID))
    val uiState: StateFlow<CuentaFormUiState> = _uiState.asStateFlow()

    init {
        if (cuentaId != SIN_ID) {
            viewModelScope.launch {
                _uiState.update { it.copy(cargando = true) }
                val cuenta = authRepository.obtenerCuenta(cuentaId)
                original = cuenta
                _uiState.update {
                    if (cuenta == null) it.copy(cargando = false, errorGeneral = "La cuenta no existe")
                    else it.copy(
                        cargando = false,
                        nombre = cuenta.nombre,
                        correo = cuenta.correo,
                        rol = RolDocente.desdeEtiqueta(cuenta.rol)
                    )
                }
            }
        }
    }

    fun onNombreChange(v: String) = _uiState.update {
        it.copy(nombre = v, nombreError = Validadores.nombrePersona(v))
    }

    fun onCorreoChange(v: String) = _uiState.update {
        it.copy(correo = v, correoError = Validadores.correo(v), errorGeneral = null)
    }

    fun onPasswordChange(v: String) = _uiState.update {
        it.copy(
            password = v,
            passwordError = validarPassword(v, it.esEdicion),
            confirmacionError = if (it.confirmacion.isNotEmpty()) Validadores.confirmarPassword(v, it.confirmacion) else null
        )
    }

    fun onConfirmacionChange(v: String) = _uiState.update {
        it.copy(confirmacion = v, confirmacionError = Validadores.confirmarPassword(it.password, v))
    }

    fun onRolChange(rol: RolDocente) = _uiState.update { it.copy(rol = rol, errorGeneral = null) }

    fun guardar() {
        val e = _uiState.value
        // Al editar con la contraseña vacía no se valida la confirmación
        val cambiaPassword = !e.esEdicion || e.password.isNotEmpty()
        val validado = e.copy(
            nombreError = Validadores.nombrePersona(e.nombre),
            correoError = Validadores.correo(e.correo),
            passwordError = validarPassword(e.password, e.esEdicion),
            confirmacionError = if (cambiaPassword) Validadores.confirmarPassword(e.password, e.confirmacion) else null
        )
        _uiState.value = validado
        val hayErrores = listOf(
            validado.nombreError, validado.correoError,
            validado.passwordError, validado.confirmacionError
        ).any { it != null }
        if (hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            val cuentaOriginal = original
            val resultado = if (cuentaOriginal != null) {
                authRepository.actualizarCuenta(cuentaOriginal, e.nombre, e.correo, e.rol, e.password)
            } else {
                authRepository.crearCuenta(e.nombre, e.correo, e.password, e.rol)
            }
            _uiState.update {
                when (resultado) {
                    is ResultadoAuth.Exito -> it.copy(guardando = false, guardado = true)
                    is ResultadoAuth.Error -> it.copy(guardando = false, errorGeneral = resultado.mensaje)
                }
            }
        }
    }

    /** Crear: contraseña obligatoria. Editar: opcional (vacía = no se cambia). */
    private fun validarPassword(valor: String, esEdicion: Boolean): String? =
        if (esEdicion && valor.isEmpty()) null else Validadores.passwordNueva(valor)

    companion object {
        private const val SIN_ID = -1L
    }
}
