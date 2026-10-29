package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CuentasUiState(
    val cuentas: List<DocenteEntity> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true
)

/** Lista de cuentas del administrador: buscar, activar/desactivar y eliminar. */
class CuentasViewModel(
    private val authRepository: AuthRepository,
    session: SessionManager
) : ViewModel() {

    /** id del administrador conectado (no puede desactivarse ni eliminarse a sí mismo). */
    val miId: Long = session.docenteId.value

    private val busqueda = MutableStateFlow("")

    val uiState: StateFlow<CuentasUiState> =
        combine(authRepository.observarCuentas(), busqueda) { lista, texto ->
            val filtrada = if (texto.isBlank()) lista else lista.filter {
                it.nombre.contains(texto, ignoreCase = true) ||
                    it.correo.contains(texto, ignoreCase = true) ||
                    it.rol.contains(texto, ignoreCase = true)
            }
            CuentasUiState(cuentas = filtrada, busqueda = texto, cargando = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CuentasUiState())

    /** Mensajes para el Snackbar (ej: "Cuenta desactivada", reglas no cumplidas). */
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto
    }

    fun cambiarActivo(cuenta: DocenteEntity, activo: Boolean) {
        viewModelScope.launch {
            val error = authRepository.cambiarActivo(cuenta, activo)
            _mensaje.value = error ?: if (activo) "Acceso de ${cuenta.nombre} activado"
            else "Acceso de ${cuenta.nombre} desactivado"
        }
    }

    fun eliminar(cuenta: DocenteEntity) {
        viewModelScope.launch {
            val error = authRepository.eliminarCuenta(cuenta)
            _mensaje.value = error ?: "Cuenta de ${cuenta.nombre} eliminada"
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}
