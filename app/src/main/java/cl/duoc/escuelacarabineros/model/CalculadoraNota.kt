package cl.duoc.escuelacarabineros.model

/**
 * Reglas de negocio de la Escuela para calcular la nota:
 * - Escala de 1,00 a 7,00 con exigencia del 60 % (4,00 = 60 % de logro).
 * - Siempre con DOS decimales y SIN aproximar (se trunca): 4,356 -> 4,35.
 */
object CalculadoraNota {

    /**
     * Porcentaje de logro exacto en "centésimas de %" (0..10000).
     * @param criterios pares (ponderación del criterio en %, nivel elegido)
     */
    fun logroCentesimas(criterios: List<Pair<Int, NivelLogro?>>): Int =
        criterios.sumOf { (ponderacion, nivel) -> ponderacion * (nivel?.porcentaje ?: 0) }

    /** Nota en centésimas (ej: 436 = 4,36), truncada a dos decimales. */
    fun notaCentesimas(logroCentesimas: Int): Int {
        val logro = logroCentesimas.coerceIn(0, 10000)
        // La división entera trunca una sola vez, sin perder precisión intermedia.
        return if (logro < 6000) {
            100 + logro / 20
        } else {
            400 + 3 * (logro - 6000) / 40
        }
    }

    /** Formato chileno con dos decimales: 600 -> "6,00". */
    fun formatear(notaCentesimas: Int): String =
        "%d,%02d".format(notaCentesimas / 100, notaCentesimas % 100)

    fun esAprobada(notaCentesimas: Int): Boolean = notaCentesimas >= 400
}
