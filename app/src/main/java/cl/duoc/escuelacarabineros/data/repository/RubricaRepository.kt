package cl.duoc.escuelacarabineros.data.repository

import cl.duoc.escuelacarabineros.data.local.dao.RubricaDao
import cl.duoc.escuelacarabineros.data.local.entity.CriterioEntity
import cl.duoc.escuelacarabineros.data.local.entity.RubricaConCriterios
import cl.duoc.escuelacarabineros.data.local.entity.RubricaEntity
import kotlinx.coroutines.flow.Flow

/** Datos de un criterio tal como llegan desde el formulario. */
data class CriterioInput(
    val descripcion: String,
    val ponderacion: Int,
    val descLogrado: String = "",
    val descAceptable: String = "",
    val descIncipiente: String = "",
    val descNoLogrado: String = ""
) {
    fun aEntidad(rubricaId: Long = 0) = CriterioEntity(
        rubricaId = rubricaId,
        descripcion = descripcion.trim(),
        ponderacion = ponderacion,
        descLogrado = descLogrado.trim(),
        descAceptable = descAceptable.trim(),
        descIncipiente = descIncipiente.trim(),
        descNoLogrado = descNoLogrado.trim()
    )
}

class RubricaRepository(private val dao: RubricaDao) {

    fun observarRubricas(): Flow<List<RubricaConCriterios>> = dao.observarTodas()

    suspend fun obtener(id: Long): RubricaConCriterios? = dao.obtenerPorId(id)

    suspend fun crear(
        nombre: String,
        asignatura: String,
        descripcion: String,
        docenteId: Long,
        criterios: List<CriterioInput>
    ): Long = dao.insertarConCriterios(
        RubricaEntity(
            nombre = nombre.trim(),
            asignatura = asignatura.trim(),
            descripcion = descripcion.trim(),
            docenteId = docenteId
        ),
        criterios.map { it.aEntidad() }
    )

    /** Edita una rúbrica existente (mantiene su id y fecha de creación). */
    suspend fun actualizar(
        original: RubricaEntity,
        nombre: String,
        asignatura: String,
        descripcion: String,
        criterios: List<CriterioInput>
    ) = dao.actualizarConCriterios(
        original.copy(
            nombre = nombre.trim(),
            asignatura = asignatura.trim(),
            descripcion = descripcion.trim()
        ),
        criterios.map { it.aEntidad(original.id) }
    )

    suspend fun eliminar(rubrica: RubricaEntity) = dao.eliminar(rubrica)

    /** Rúbrica de ejemplo (contenido ficticio) para que la app no parta vacía. */
    suspend fun crearRubricaDemoSiVacia(docenteId: Long) {
        if (dao.contar() > 0) return
        crear(
            nombre = "Control vehicular preventivo",
            asignatura = "Técnicas y Tácticas Policiales",
            descripcion = "Evaluación práctica de aproximación y control de un vehículo con medidas de seguridad.",
            docenteId = docenteId,
            criterios = listOf(
                CriterioInput(
                    "Aproximación al vehículo con medidas de seguridad", 30,
                    descLogrado = "Se aproxima por el ángulo correcto, mantiene distancia y cubre a su compañero.",
                    descAceptable = "Se aproxima correctamente, pero descuida la distancia o la cobertura.",
                    descIncipiente = "Se aproxima sin considerar la mayoría de las medidas de seguridad.",
                    descNoLogrado = "No aplica medidas de seguridad en la aproximación."
                ),
                CriterioInput(
                    "Comunicación verbal con el conductor", 20,
                    descLogrado = "Se identifica, saluda y explica el motivo del control con claridad y respeto.",
                    descAceptable = "Se comunica con respeto, pero omite identificarse o explicar el motivo.",
                    descIncipiente = "La comunicación es confusa o poco respetuosa.",
                    descNoLogrado = "No establece comunicación adecuada con el conductor."
                ),
                CriterioInput(
                    "Solicitud y revisión de documentos", 20,
                    descLogrado = "Solicita y revisa licencia, padrón y seguro sin perder de vista al conductor.",
                    descAceptable = "Revisa los documentos, pero pierde contacto visual con el conductor.",
                    descIncipiente = "Revisa solo parte de la documentación.",
                    descNoLogrado = "No solicita la documentación."
                ),
                CriterioInput(
                    "Registro preventivo según procedimiento", 30,
                    descLogrado = "Realiza el registro completo siguiendo el procedimiento institucional.",
                    descAceptable = "Realiza el registro con omisiones menores.",
                    descIncipiente = "El registro es incompleto o desordenado.",
                    descNoLogrado = "No realiza el registro preventivo."
                )
            )
        )
    }
}
