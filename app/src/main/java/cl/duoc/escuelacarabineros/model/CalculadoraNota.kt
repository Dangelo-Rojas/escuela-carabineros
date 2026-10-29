package cl.duoc.escuelacarabineros.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Reglas de negocio de la Escuela para calcular la nota:
 * - Escala de 1,00 a 7,00 con exigencia del 60 % (4,00 = 60 % de logro).
 * - Siempre con DOS decimales y SIN aproximar (se trunca): 4,356 -> 4,35.
 */
object CalculadoraNota {

    private val EXIGENCIA = BigDecimal("0.60")
    private val UNO = BigDecimal.ONE
    private val TRES = BigDecimal("3")
    private val CUATRO = BigDecimal("4")

    /**
     * Porcentaje de logro exacto en "centésimas de %" (0..10000).
     * @param criterios pares (ponderación del criterio en %, nivel elegido)
     */
    fun logroCentesimas(criterios: List<Pair<Int, NivelLogro?>>): Int =
        criterios.sumOf { (ponderacion, nivel) -> ponderacion * (nivel?.porcentaje ?: 0) }

    /** Nota en centésimas (ej: 436 = 4,36), truncada a dos decimales. */
    fun notaCentesimas(logroCentesimas: Int): Int {
        val logro = BigDecimal(logroCentesimas.coerceIn(0, 10000))
            .divide(BigDecimal("10000"), 10, RoundingMode.DOWN)

        val nota = if (logro < EXIGENCIA) {
            // 1 + 3 * (logro / 0,6)
            UNO + TRES * logro.divide(EXIGENCIA, 10, RoundingMode.DOWN)
        } else {
            // 4 + 3 * ((logro - 0,6) / 0,4)
            CUATRO + TRES * (logro - EXIGENCIA).divide(UNO - EXIGENCIA, 10, RoundingMode.DOWN)
        }
        return nota.setScale(2, RoundingMode.DOWN).movePointRight(2).toInt()
    }

    /** Formato chileno con dos decimales: 600 -> "6,00". */
    fun formatear(notaCentesimas: Int): String =
        "%d,%02d".format(notaCentesimas / 100, notaCentesimas % 100)

    fun esAprobada(notaCentesimas: Int): Boolean = notaCentesimas >= 400
}
