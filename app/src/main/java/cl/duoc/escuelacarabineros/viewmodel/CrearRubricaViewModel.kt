package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.escuelacarabineros.data.local.entity.RubricaEntity
import cl.duoc.escuelacarabineros.data.repository.CriterioInput
import cl.duoc.escuelacarabineros.data.repository.RubricaRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import cl.duoc.escuelacarabineros.model.NivelLogro
import cl.duoc.escuelacarabineros.navigation.Rutas
import cl.duoc.escuelacarabineros.utils.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un criterio mientras se edita en el formulario. */
data class CriterioForm(
    val idLocal: Long,
    val descripcion: String = "",
    val ponderacion: String = "",
    /** Qué significa cada nivel de logro en este criterio (opcional). */
    val descNiveles: Map<NivelLogro, String> = emptyMap(),
    val descripcionError: String? = null,
    val ponderacionError: String? = null,
    val descNivelesError: Map<NivelLogro, String> = emptyMap()
)

data class CrearRubricaUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val asignatura: String = "",
    val descripcion: String = "",
    val criterios: List<CriterioForm> = listOf(CriterioForm(idLocal = 1)),
    val nombreError: String? = null,
    val asignaturaError: String? = null,
    val descripcionError: String? = null,
    val criteriosError: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val sumaPonderaciones: Int
        get() = criterios.sumOf { it.ponderacion.toIntOrNull() ?: 0 }
}

/**
 * ViewModel del formulario de rúbricas. Sirve para CREAR (sin argumento)
 * y para EDITAR (cuando la ruta trae el id de la rúbrica).
 */
class CrearRubricaViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RubricaRepository,
    private val session: SessionManager
) : ViewModel() {

    private val rubricaId: Long = savedStateHandle.get<Long>(Rutas.ARG_RUBRICA_ID) ?: SIN_ID
    private var original: RubricaEntity? = null

    private val _uiState = MutableStateFlow(CrearRubricaUiState(esEdicion = rubricaId != SIN_ID))
    val uiState: StateFlow<CrearRubricaUiState> = _uiState.asStateFlow()

    private var siguienteId = 2L

    init {
        if (rubricaId != SIN_ID) cargarParaEditar()
    }

    private fun cargarParaEditar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val datos = repository.obtener(rubricaId)
            if (datos == null) {
                _uiState.update { it.copy(cargando = false) }
                return@launch
            }
            original = datos.rubrica
            val criterios = datos.criterios.map { c ->
                CriterioForm(
                    idLocal = siguienteId++,
                    descripcion = c.descripcion,
                    ponderacion = c.ponderacion.toString(),
                    descNiveles = mapOf(
                        NivelLogro.LOGRADO to c.descLogrado,
                        NivelLogro.ACEPTABLE to c.descAceptable,
                        NivelLogro.INCIPIENTE to c.descIncipiente,
                        NivelLogro.NO_LOGRADO to c.descNoLogrado
                    )
                )
            }
            _uiState.update {
                it.copy(
                    cargando = false,
                    nombre = datos.rubrica.nombre,
                    asignatura = datos.rubrica.asignatura,
                    descripcion = datos.rubrica.descripcion,
                    criterios = criterios.ifEmpty { listOf(CriterioForm(idLocal = siguienteId++)) }
                )
            }
        }
    }

    fun onNombreChange(v: String) = _uiState.update {
        it.copy(nombre = v, nombreError = Validadores.textoObligatorio(v, "El nombre"))
    }

    fun onAsignaturaChange(v: String) = _uiState.update {
        it.copy(asignatura = v, asignaturaError = Validadores.textoObligatorio(v, "La asignatura"))
    }

    fun onDescripcionChange(v: String) = _uiState.update {
        it.copy(descripcion = v, descripcionError = Validadores.textoOpcional(v, 300))
    }

    fun agregarCriterio() = _uiState.update {
        if (it.criterios.size >= MAX_CRITERIOS) {
            it.copy(criteriosError = "Máximo $MAX_CRITERIOS criterios por rúbrica")
        } else {
            it.copy(criterios = it.criterios + CriterioForm(idLocal = siguienteId++), criteriosError = null)
        }
    }

    fun eliminarCriterio(idLocal: Long) = _uiState.update {
        if (it.criterios.size <= 1) it.copy(criteriosError = "La rúbrica necesita al menos un criterio")
        else it.copy(criterios = it.criterios.filterNot { c -> c.idLocal == idLocal }, criteriosError = null)
    }

    fun onCriterioDescripcionChange(idLocal: Long, v: String) = actualizarCriterio(idLocal) {
        it.copy(descripcion = v, descripcionError = Validadores.textoObligatorio(v, "El criterio", max = 120))
    }

    fun onCriterioPonderacionChange(idLocal: Long, v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(3)
        actualizarCriterio(idLocal) {
            it.copy(ponderacion = soloDigitos, ponderacionError = Validadores.ponderacion(soloDigitos))
        }
        _uiState.update { it.copy(criteriosError = null) }
    }

    fun onDescNivelChange(idLocal: Long, nivel: NivelLogro, v: String) = actualizarCriterio(idLocal) {
        val error = Validadores.textoOpcional(v, MAX_DESC_NIVEL)
        it.copy(
            descNiveles = it.descNiveles + (nivel to v),
            descNivelesError = if (error == null) it.descNivelesError - nivel
            else it.descNivelesError + (nivel to error)
        )
    }

    /** Reparte 100 % en partes iguales (el resto se suma al primero). */
    fun repartirPonderacionEquitativa() = _uiState.update {
        val n = it.criterios.size
        val base = 100 / n
        val resto = 100 - base * n
        it.copy(
            criterios = it.criterios.mapIndexed { i, c ->
                val valor = (base + if (i == 0) resto else 0).toString()
                c.copy(ponderacion = valor, ponderacionError = null)
            },
            criteriosError = null
        )
    }

    fun guardar() {
        val e = _uiState.value
        val criteriosValidados = e.criterios.map { c ->
            c.copy(
                descripcionError = Validadores.textoObligatorio(c.descripcion, "El criterio", max = 120),
                ponderacionError = Validadores.ponderacion(c.ponderacion),
                descNivelesError = c.descNiveles.mapNotNull { (nivel, texto) ->
                    Validadores.textoOpcional(texto, MAX_DESC_NIVEL)?.let { nivel to it }
                }.toMap()
            )
        }
        val validado = e.copy(
            nombreError = Validadores.textoObligatorio(e.nombre, "El nombre"),
            asignaturaError = Validadores.textoObligatorio(e.asignatura, "La asignatura"),
            descripcionError = Validadores.textoOpcional(e.descripcion, 300),
            criterios = criteriosValidados,
            criteriosError = Validadores.sumaPonderaciones(criteriosValidados.sumOf { it.ponderacion.toIntOrNull() ?: 0 })
        )
        _uiState.value = validado

        val hayErrores = listOf(validado.nombreError, validado.asignaturaError, validado.descripcionError, validado.criteriosError)
            .any { it != null } ||
            criteriosValidados.any {
                it.descripcionError != null || it.ponderacionError != null || it.descNivelesError.isNotEmpty()
            }
        if (hayErrores) return

        val inputs = criteriosValidados.map { c ->
            CriterioInput(
                descripcion = c.descripcion,
                ponderacion = c.ponderacion.toInt(),
                descLogrado = c.descNiveles[NivelLogro.LOGRADO].orEmpty(),
                descAceptable = c.descNiveles[NivelLogro.ACEPTABLE].orEmpty(),
                descIncipiente = c.descNiveles[NivelLogro.INCIPIENTE].orEmpty(),
                descNoLogrado = c.descNiveles[NivelLogro.NO_LOGRADO].orEmpty()
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val rubricaOriginal = original
            if (rubricaOriginal != null) {
                repository.actualizar(rubricaOriginal, validado.nombre, validado.asignatura, validado.descripcion, inputs)
            } else {
                repository.crear(
                    nombre = validado.nombre,
                    asignatura = validado.asignatura,
                    descripcion = validado.descripcion,
                    docenteId = session.docenteId.value,
                    criterios = inputs
                )
            }
            _uiState.update { it.copy(guardando = false, guardado = true) }
        }
    }

    private fun actualizarCriterio(idLocal: Long, cambio: (CriterioForm) -> CriterioForm) =
        _uiState.update { estado ->
            estado.copy(criterios = estado.criterios.map { if (it.idLocal == idLocal) cambio(it) else it })
        }

    companion object {
        const val MAX_CRITERIOS = 10
        const val MAX_DESC_NIVEL = 150
        private const val SIN_ID = -1L
    }
}
