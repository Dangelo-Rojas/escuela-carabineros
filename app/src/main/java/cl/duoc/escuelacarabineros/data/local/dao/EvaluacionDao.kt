package cl.duoc.escuelacarabineros.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionConRubrica
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluacionDao {

    @Insert
    suspend fun insertar(evaluacion: EvaluacionEntity): Long

    @Delete
    suspend fun eliminar(evaluacion: EvaluacionEntity)

    @Query(
        """
        SELECT e.*, r.nombre AS nombreRubrica
        FROM evaluaciones e
        INNER JOIN rubricas r ON r.id = e.rubricaId
        ORDER BY e.fecha DESC
        """
    )
    fun observarConRubrica(): Flow<List<EvaluacionConRubrica>>
}
