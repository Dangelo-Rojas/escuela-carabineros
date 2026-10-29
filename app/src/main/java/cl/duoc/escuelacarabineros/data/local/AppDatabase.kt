package cl.duoc.escuelacarabineros.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.escuelacarabineros.data.local.dao.DocenteDao
import cl.duoc.escuelacarabineros.data.local.dao.EvaluacionDao
import cl.duoc.escuelacarabineros.data.local.dao.RubricaDao
import cl.duoc.escuelacarabineros.data.local.entity.CriterioEntity
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionEntity
import cl.duoc.escuelacarabineros.data.local.entity.RubricaEntity

/** Base de datos local (Room / SQLite). Persiste todo en el dispositivo. */
@Database(
    entities = [
        DocenteEntity::class,
        RubricaEntity::class,
        CriterioEntity::class,
        EvaluacionEntity::class
    ],
    version = 3, // v3: rol administrador y cuentas activas/inactivas
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao
    abstract fun rubricaDao(): RubricaDao
    abstract fun evaluacionDao(): EvaluacionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rubricas_esfocar.db"
                )
                    // Prototipo con datos ficticios: si cambia el esquema se recrea la BD
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { instancia = it }
            }
    }
}
