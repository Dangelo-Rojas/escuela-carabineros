package cl.duoc.escuelacarabineros.data.repository

import cl.duoc.escuelacarabineros.data.local.dao.EvaluacionDao
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionConRubrica
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionEntity
import cl.duoc.escuelacarabineros.utils.FotoStorage
import kotlinx.coroutines.flow.Flow

class EvaluacionRepository(
    private val dao: EvaluacionDao,
    private val fotos: FotoStorage
) {

    fun observarHistorial(): Flow<List<EvaluacionConRubrica>> = dao.observarConRubrica()

    suspend fun guardar(evaluacion: EvaluacionEntity): Long = dao.insertar(evaluacion)

    suspend fun eliminar(evaluacion: EvaluacionEntity) {
        dao.eliminar(evaluacion)
        fotos.eliminar(evaluacion.fotoPath)
    }
}
