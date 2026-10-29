package cl.duoc.escuelacarabineros.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocenteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(docente: DocenteEntity): Long

    @Update
    suspend fun actualizar(docente: DocenteEntity)

    @Delete
    suspend fun eliminar(docente: DocenteEntity)

    @Query("SELECT * FROM docentes WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): DocenteEntity?

    @Query("SELECT * FROM docentes WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): DocenteEntity?

    @Query("SELECT COUNT(*) FROM docentes")
    suspend fun contar(): Int

    /** Lista de cuentas para la pantalla del administrador (se actualiza sola). */
    @Query("SELECT * FROM docentes ORDER BY rol, nombre")
    fun observarTodos(): Flow<List<DocenteEntity>>

    /** Para impedir que la app se quede sin ningún administrador activo. */
    @Query("SELECT COUNT(*) FROM docentes WHERE rol = 'Administrador' AND activo = 1")
    suspend fun contarAdministradoresActivos(): Int
}
