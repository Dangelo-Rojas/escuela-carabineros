package cl.duoc.escuelacarabineros.utils

/**
 * Reglas de validación centralizadas. Devuelven null si el valor es válido
 * o el mensaje de error que se muestra bajo el campo.
 * Las usan los ViewModels (la vista solo muestra el resultado).
 */
object Validadores {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor.trim()) -> "Ingresa un correo válido (ej: nombre@dominio.cl)"
        else -> null
    }

    fun passwordLogin(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        else -> null
    }

    fun passwordNueva(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Debe tener al menos 6 caracteres"
        !valor.any { it.isDigit() } || !valor.any { it.isLetter() } ->
            "Debe combinar letras y números"
        else -> null
    }

    fun confirmarPassword(password: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Confirma tu contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun nombrePersona(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !valor.all { it.isLetter() || it.isWhitespace() } -> "Solo se permiten letras"
        else -> null
    }

    fun textoObligatorio(valor: String, campo: String, min: Int = 3, max: Int = 80): String? = when {
        valor.isBlank() -> "$campo es obligatorio"
        valor.trim().length < min -> "Debe tener al menos $min caracteres"
        valor.length > max -> "Máximo $max caracteres"
        else -> null
    }

    fun textoOpcional(valor: String, max: Int): String? =
        if (valor.length > max) "Máximo $max caracteres (llevas ${valor.length})" else null

    /** Ponderación de un criterio: número entero entre 1 y 100. */
    fun ponderacion(valor: String): String? {
        val numero = valor.toIntOrNull()
        return when {
            valor.isBlank() -> "Obligatorio"
            numero == null -> "Solo números"
            numero !in 1..100 -> "Entre 1 y 100"
            else -> null
        }
    }

    fun sede(valor: String): String? =
        if (valor.isBlank()) "Selecciona la sede donde se realiza la evaluación" else null

    fun sumaPonderaciones(suma: Int): String? =
        if (suma != 100) "Las ponderaciones deben sumar 100 % (llevas $suma %)" else null
}
