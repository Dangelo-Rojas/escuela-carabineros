package cl.duoc.escuelacarabineros.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Docente o instructor que usa la app (los alumnos no inician sesión). */
@Entity(
    tableName = "docentes",
    indices = [Index(value = ["correo"], unique = true)]
)
data class DocenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: String, // "Docente", "Instructor" o "Administrador"
    val activo: Boolean = true // false = acceso revocado (no puede iniciar sesión)
)

/** Rúbrica (instrumento de evaluación) creada por un docente. */
@Entity(tableName = "rubricas")
data class RubricaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val asignatura: String,
    val descripcion: String,
    val docenteId: Long,
    val fechaCreacion: Long = System.currentTimeMillis()
)

/** Criterio (indicador) de una rúbrica, con su ponderación en %. */
@Entity(
    tableName = "criterios",
    foreignKeys = [
        ForeignKey(
            entity = RubricaEntity::class,
            parentColumns = ["id"],
            childColumns = ["rubricaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rubricaId")]
)
data class CriterioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rubricaId: Long,
    val descripcion: String,
    val ponderacion: Int, // porcentaje, la suma de una rúbrica debe ser 100
    // Qué significa cada nivel de logro en ESTE criterio (retroalimentación para el alumno)
    val descLogrado: String = "",
    val descAceptable: String = "",
    val descIncipiente: String = "",
    val descNoLogrado: String = ""
)

/**
 * Evaluación aplicada a un alumno o grupo.
 * La nota se guarda en centésimas (4,36 -> 436) para no perder los dos decimales
 * ni redondear, tal como pide la Escuela.
 */
@Entity(
    tableName = "evaluaciones",
    foreignKeys = [
        ForeignKey(
            entity = RubricaEntity::class,
            parentColumns = ["id"],
            childColumns = ["rubricaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rubricaId")]
)
data class EvaluacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rubricaId: Long,
    val docenteId: Long,
    val evaluado: String,
    val tipo: String, // "Individual" o "Grupal"
    val sede: String = "", // sede de la Escuela donde se evaluó
    val notaCentesimas: Int,
    val porcentajeLogro: Int, // 0..100
    val detalle: String, // "criterio|nivel" separados por salto de línea
    val comentario: String,
    val fotoPath: String?,
    val latitud: Double?,
    val longitud: Double?,
    val fecha: Long = System.currentTimeMillis()
)

/** Rúbrica junto a sus criterios (relación 1 a N). */
data class RubricaConCriterios(
    @Embedded val rubrica: RubricaEntity,
    @Relation(parentColumn = "id", entityColumn = "rubricaId")
    val criterios: List<CriterioEntity>
)

/** Evaluación junto al nombre de la rúbrica usada (para el historial). */
data class EvaluacionConRubrica(
    @Embedded val evaluacion: EvaluacionEntity,
    val nombreRubrica: String
)
