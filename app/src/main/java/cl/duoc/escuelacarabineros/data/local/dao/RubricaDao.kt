package cl.duoc.escuelacarabineros.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.duoc.escuelacarabineros.data.local.entity.CriterioEntity
import cl.duoc.escuelacarabineros.data.local.entity.RubricaConCriterios
import cl.duoc.escuelacarabineros.data.local.entity.RubricaEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RubricaDao {

    @Transaction
    @Query("SELECT * FROM rubricas ORDER BY fechaCreacion DESC")
    abstract fun observarTodas(): Flow<List<RubricaConCriterios>>

    @Transaction
    @Query("SELECT * FROM rubricas WHERE id = :id LIMIT 1")
    abstract suspend fun obtenerPorId(id: Long): RubricaConCriterios?

    @Insert
    abstract suspend fun insertarRubrica(rubrica: RubricaEntity): Long

    @Insert
    abstract suspend fun insertarCriterios(criterios: List<CriterioEntity>)

    @Update
    abstract suspend fun actualizarRubrica(rubrica: RubricaEntity)

    @Query("DELETE FROM criterios WHERE rubricaId = :rubricaId")
    abstract suspend fun eliminarCriterios(rubricaId: Long)

    @Delete
    abstract suspend fun eliminar(rubrica: RubricaEntity)

    @Query("SELECT COUNT(*) FROM rubricas")
    abstract suspend fun contar(): Int

    /** Inserta la rúbrica y sus criterios en una sola transacción. */
    @Transaction
    open suspend fun insertarConCriterios(
        rubrica: RubricaEntity,
        criterios: List<CriterioEntity>
    ): Long {
        val id = insertarRubrica(rubrica)
        insertarCriterios(criterios.map { it.copy(rubricaId = id) })
        return id
    }

    /** Edita la rúbrica: actualiza sus datos y reemplaza sus criterios. */
    @Transaction
    open suspend fun actualizarConCriterios(
        rubrica: RubricaEntity,
        criterios: List<CriterioEntity>
    ) {
        actualizarRubrica(rubrica)
        eliminarCriterios(rubrica.id)
        insertarCriterios(criterios.map { it.copy(id = 0, rubricaId = rubrica.id) })
    }
}
