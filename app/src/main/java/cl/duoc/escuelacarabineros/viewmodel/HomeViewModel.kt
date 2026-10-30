package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.RubricaConCriterios
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.repository.RubricaRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val rubricas: List<RubricaConCriterios> = emptyList(),
    val cargando: Boolean = true
)

class HomeViewModel(
    private val rubricaRepository: RubricaRepository,
    private val authRepository: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    val nombreDocente: String get() = session.nombreDocente

    /** Solo el Administrador ve la sección de cuentas. */
    val esAdministrador: Boolean get() = session.esAdministrador

    val uiState: StateFlow<HomeUiState> = rubricaRepository.observarRubricas()
        .map { HomeUiState(rubricas = it, cargando = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun eliminar(rubrica: RubricaConCriterios) {
        viewModelScope.launch { rubricaRepository.eliminar(rubrica.rubrica) }
    }

    fun cerrarSesion() = authRepository.cerrarSesion()
}
