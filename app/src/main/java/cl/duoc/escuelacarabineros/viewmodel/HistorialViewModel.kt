package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionConRubrica
import cl.duoc.escuelacarabineros.data.repository.EvaluacionRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import cl.duoc.escuelacarabineros.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class HistorialUiState(
    val evaluaciones: List<EvaluacionConRubrica> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true
)

class HistorialViewModel(
    private val repository: EvaluacionRepository,
    private val pdfGenerator: PdfGenerator,
    private val session: SessionManager
) : ViewModel() {

    private val busqueda = MutableStateFlow("")

    val uiState: StateFlow<HistorialUiState> =
        combine(repository.observarHistorial(), busqueda) { lista, texto ->
            val filtrada = if (texto.isBlank()) lista else lista.filter {
                it.evaluacion.evaluado.contains(texto, ignoreCase = true) ||
                    it.nombreRubrica.contains(texto, ignoreCase = true) ||
                    it.evaluacion.sede.contains(texto, ignoreCase = true)
            }
            HistorialUiState(evaluaciones = filtrada, busqueda = texto, cargando = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    /** id de la evaluación cuyo PDF se está generando (para mostrar el indicador de carga). */
    private val _generandoPdfId = MutableStateFlow<Long?>(null)
    val generandoPdfId: StateFlow<Long?> = _generandoPdfId.asStateFlow()

    /** PDF listo para compartir; la pantalla lo consume y llama a [pdfCompartido]. */
    private val _pdfListo = MutableStateFlow<File?>(null)
    val pdfListo: StateFlow<File?> = _pdfListo.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onBusquedaChange(texto: String) {
        busqueda.value = texto
    }

    fun generarPdf(item: EvaluacionConRubrica) {
        if (_generandoPdfId.value != null) return
        viewModelScope.launch {
            _generandoPdfId.value = item.evaluacion.id
            try {
                _pdfListo.value = pdfGenerator.generar(item, session.nombreDocente)
            } catch (e: Exception) {
                _error.value = "No se pudo generar el PDF"
            } finally {
                _generandoPdfId.value = null
            }
        }
    }

    fun pdfCompartido() {
        _pdfListo.value = null
    }

    fun errorMostrado() {
        _error.value = null
    }

    fun eliminar(item: EvaluacionConRubrica) {
        viewModelScope.launch { repository.eliminar(item.evaluacion) }
    }
}
