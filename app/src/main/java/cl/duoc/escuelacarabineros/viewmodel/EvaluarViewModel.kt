package cl.duoc.escuelacarabineros.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionEntity
import cl.duoc.escuelacarabineros.data.local.entity.RubricaConCriterios
import cl.duoc.escuelacarabineros.data.repository.EvaluacionRepository
import cl.duoc.escuelacarabineros.data.repository.RubricaRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import cl.duoc.escuelacarabineros.model.CalculadoraNota
import cl.duoc.escuelacarabineros.model.NivelLogro
import cl.duoc.escuelacarabineros.model.TipoEvaluacion
import cl.duoc.escuelacarabineros.navigation.Rutas
import cl.duoc.escuelacarabineros.utils.FotoStorage
import cl.duoc.escuelacarabineros.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EvaluarUiState(
    val rubrica: RubricaConCriterios? = null,
    val cargando: Boolean = true,
    val evaluado: String = "",
    val tipo: TipoEvaluacion = TipoEvaluacion.INDIVIDUAL,
    val sede: String = "",
    val sedeError: String? = null,
    val niveles: Map<Long, NivelLogro> = emptyMap(), // criterioId -> nivel elegido
    val comentario: String = "",
    val fotoPath: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val obteniendoUbicacion: Boolean = false,
    val evaluadoError: String? = null,
    val nivelesError: String? = null,
    val comentarioError: String? = null,
    val mensaje: String? = null, // mensajes para Snackbar (permisos, GPS, etc.)
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val logroCentesimas: Int
        get() = CalculadoraNota.logroCentesimas(
            rubrica?.criterios.orEmpty().map { it.ponderacion to niveles[it.id] }
        )
    val notaCentesimas: Int get() = CalculadoraNota.notaCentesimas(logroCentesimas)
    val notaTexto: String get() = CalculadoraNota.formatear(notaCentesimas)
    val criteriosEvaluados: Int get() = niveles.size
    val totalCriterios: Int get() = rubrica?.criterios?.size ?: 0
}

class EvaluarViewModel(
    savedStateHandle: SavedStateHandle,
    private val rubricaRepository: RubricaRepository,
    private val evaluacionRepository: EvaluacionRepository,
    private val fotoStorage: FotoStorage,
    private val session: SessionManager
) : ViewModel() {

    private val rubricaId: Long = checkNotNull(savedStateHandle[Rutas.ARG_RUBRICA_ID])

    // La sede parte con la última usada por el docente
    private val _uiState = MutableStateFlow(EvaluarUiState(sede = session.ultimaSede))
    val uiState: StateFlow<EvaluarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val rubrica = rubricaRepository.obtener(rubricaId)
            _uiState.update { it.copy(rubrica = rubrica, cargando = false) }
        }
    }

    fun onEvaluadoChange(v: String) = _uiState.update {
        it.copy(evaluado = v, evaluadoError = validarEvaluado(v, it.tipo))
    }

    fun onTipoChange(tipo: TipoEvaluacion) = _uiState.update {
        it.copy(tipo = tipo, evaluadoError = if (it.evaluado.isEmpty()) null else validarEvaluado(it.evaluado, tipo))
    }

    fun onSedeChange(sede: String) = _uiState.update {
        it.copy(sede = sede, sedeError = Validadores.sede(sede))
    }

    fun onNivelSeleccionado(criterioId: Long, nivel: NivelLogro) = _uiState.update {
        it.copy(niveles = it.niveles + (criterioId to nivel), nivelesError = null)
    }

    fun onComentarioChange(v: String) = _uiState.update {
        it.copy(comentario = v, comentarioError = Validadores.textoOpcional(v, MAX_COMENTARIO))
    }

    // ---------- Recursos nativos ----------

    fun onFotoTomada(bitmap: Bitmap) {
        viewModelScope.launch {
            fotoStorage.eliminar(_uiState.value.fotoPath) // reemplaza la anterior
            val ruta = fotoStorage.guardar(bitmap)
            _uiState.update { it.copy(fotoPath = ruta, mensaje = "Foto de evidencia agregada") }
        }
    }

    fun quitarFoto() {
        fotoStorage.eliminar(_uiState.value.fotoPath)
        _uiState.update { it.copy(fotoPath = null) }
    }

    fun onBuscandoUbicacion() = _uiState.update { it.copy(obteniendoUbicacion = true) }

    fun onUbicacionObtenida(latitud: Double?, longitud: Double?) = _uiState.update {
        if (latitud == null || longitud == null) {
            it.copy(obteniendoUbicacion = false, mensaje = "No se pudo obtener la ubicación. Revisa que el GPS esté activo.")
        } else {
            it.copy(obteniendoUbicacion = false, latitud = latitud, longitud = longitud, mensaje = "Ubicación registrada")
        }
    }

    fun onPermisoDenegado(recurso: String) = _uiState.update {
        it.copy(obteniendoUbicacion = false, mensaje = "Permiso de $recurso denegado. Puedes habilitarlo en Ajustes.")
    }

    fun onGpsDesactivado() = _uiState.update {
        it.copy(mensaje = "Activa la ubicación (GPS) del dispositivo para registrar el lugar")
    }

    fun mensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    // ---------- Guardar ----------

    fun guardar() {
        val e = _uiState.value
        val rubrica = e.rubrica ?: return
        val evaluadoError = validarEvaluado(e.evaluado, e.tipo)
        val sedeError = Validadores.sede(e.sede)
        val faltantes = e.totalCriterios - e.criteriosEvaluados
        val nivelesError = if (faltantes > 0) "Falta evaluar $faltantes criterio(s)" else null
        val comentarioError = Validadores.textoOpcional(e.comentario, MAX_COMENTARIO)

        _uiState.update {
            it.copy(
                evaluadoError = evaluadoError,
                sedeError = sedeError,
                nivelesError = nivelesError,
                comentarioError = comentarioError
            )
        }
        if (listOf(evaluadoError, sedeError, nivelesError, comentarioError).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val detalle = rubrica.criterios.joinToString("\n") { c ->
                "${c.descripcion} (${c.ponderacion} %)|${e.niveles[c.id]?.etiqueta}"
            }
            evaluacionRepository.guardar(
                EvaluacionEntity(
                    rubricaId = rubrica.rubrica.id,
                    docenteId = session.docenteId.value,
                    evaluado = e.evaluado.trim(),
                    tipo = e.tipo.etiqueta,
                    sede = e.sede,
                    notaCentesimas = e.notaCentesimas,
                    porcentajeLogro = e.logroCentesimas / 100,
                    detalle = detalle,
                    comentario = e.comentario.trim(),
                    fotoPath = e.fotoPath,
                    latitud = e.latitud,
                    longitud = e.longitud
                )
            )
            session.ultimaSede = e.sede
            _uiState.update { it.copy(guardando = false, guardado = true) }
        }
    }

    private fun validarEvaluado(valor: String, tipo: TipoEvaluacion): String? =
        if (tipo == TipoEvaluacion.INDIVIDUAL) Validadores.nombrePersona(valor)
        else Validadores.textoObligatorio(valor, "El nombre del grupo", min = 2, max = 60)

    companion object {
        const val MAX_COMENTARIO = 500
    }
}
