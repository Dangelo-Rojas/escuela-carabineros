package cl.duoc.escuelacarabineros.model

/**
 * Niveles de logro de un criterio. El porcentaje indica cuánto de la
 * ponderación del criterio obtiene el evaluado.
 */
enum class NivelLogro(val etiqueta: String, val porcentaje: Int) {
    LOGRADO("Logrado", 100),
    ACEPTABLE("Aceptable", 60),
    INCIPIENTE("Incipiente", 30),
    NO_LOGRADO("No logrado", 0);

    companion object {
        fun desdeNombre(nombre: String): NivelLogro? = entries.firstOrNull { it.name == nombre }
    }
}

/** Tipo de evaluación: a un alumno o a un grupo de trabajo. */
enum class TipoEvaluacion(val etiqueta: String) {
    INDIVIDUAL("Individual"),
    GRUPAL("Grupal")
}

/** Rol del usuario de la app. Solo el Administrador gestiona cuentas. */
enum class RolDocente(val etiqueta: String) {
    DOCENTE("Docente"),
    INSTRUCTOR("Instructor"),
    ADMINISTRADOR("Administrador");

    companion object {
        fun desdeEtiqueta(etiqueta: String): RolDocente =
            entries.firstOrNull { it.etiqueta == etiqueta } ?: DOCENTE
    }
}
