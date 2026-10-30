package cl.duoc.escuelacarabineros

import cl.duoc.escuelacarabineros.model.CalculadoraNota
import cl.duoc.escuelacarabineros.model.NivelLogro
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.*
import org.junit.Test

class CalculadoraNotaTest {

    @Test
    fun todoLogrado_daNota7() {
        val logro = CalculadoraNota.logroCentesimas(
            listOf(
                30 to NivelLogro.LOGRADO,
                20 to NivelLogro.LOGRADO,
                20 to NivelLogro.LOGRADO,
                30 to NivelLogro.LOGRADO
            )
        )
        assertEquals(10000, logro)
        assertEquals(700, CalculadoraNota.notaCentesimas(logro))
    }

    @Test
    fun criterioSinEvaluar_cuentaComoCero() {
        val logro = CalculadoraNota.logroCentesimas(
            listOf(
                30 to NivelLogro.LOGRADO,
                20 to NivelLogro.ACEPTABLE,
                20 to NivelLogro.INCIPIENTE,
                30 to null
            )
        )
        assertEquals(4800, logro)
        assertEquals(340, CalculadoraNota.notaCentesimas(logro))
    }

    @Test
    fun extremosDeLaEscala() {
        assertEquals(100, CalculadoraNota.notaCentesimas(0))
        assertEquals(400, CalculadoraNota.notaCentesimas(6000))
        assertEquals(700, CalculadoraNota.notaCentesimas(12000))
    }

    @Test
    fun notaSeTruncaSinRedondear() {
        assertEquals(407, CalculadoraNota.notaCentesimas(6100))
        assertEquals(325, CalculadoraNota.notaCentesimas(4500))
    }

    @Test
    fun formatoChileno() {
        assertEquals("6,00", CalculadoraNota.formatear(600))
        assertEquals("4,36", CalculadoraNota.formatear(436))
        assertEquals("4,05", CalculadoraNota.formatear(405))
    }

    @Test
    fun nota399_noAprueba() {
        assertFalse(CalculadoraNota.esAprobada(399))
        assertTrue(CalculadoraNota.esAprobada(400))
    }

    @Test
    fun logro1PorCiento_daNota105() {
        assertEquals(105, CalculadoraNota.notaCentesimas(100))
    }

    @Test
    fun logro20PorCiento_daNota200() {
        assertEquals(200, CalculadoraNota.notaCentesimas(2000))
    }

    @Test
    fun logro40PorCiento_daNota300() {
        assertEquals(300, CalculadoraNota.notaCentesimas(4000))
    }

    @Test
    fun logro50PorCiento_daNota350() {
        assertEquals(350, CalculadoraNota.notaCentesimas(5000))
    }

    @Test
    fun logro60PorCiento_daNota400() {
        assertEquals(400, CalculadoraNota.notaCentesimas(6000))
    }

    @Test
    fun logro100PorCiento_daNota700() {
        assertEquals(700, CalculadoraNota.notaCentesimas(10000))
    }

    @Test
    fun todosLosLogros_coincidenConReferenciaExacta_yConservanEscalaYAprobacion() {
        val discrepancias = mutableListOf<String>()
        for (logro in 0..10000) {
            val esperada = notaReferencia(logro)
            val actual = CalculadoraNota.notaCentesimas(logro)
            if (actual != esperada) {
                discrepancias += "logro=$logro: esperada=$esperada, actual=$actual"
            }
            assertTrue("Fuera de escala para logro=$logro", actual in 100..700)
            assertEquals(
                "Aprobación incorrecta para logro=$logro",
                logro >= 6000,
                CalculadoraNota.esAprobada(actual)
            )
        }
        assertTrue(
            "${discrepancias.size} discrepancias en 10001 valores: ${discrepancias.take(5)}",
            discrepancias.isEmpty()
        )
    }

    @Test
    fun valoresFueraDeRango_seLimitanAntesDeCalcular() {
        assertEquals(100, CalculadoraNota.notaCentesimas(Int.MIN_VALUE))
        assertEquals(100, CalculadoraNota.notaCentesimas(-1))
        assertEquals(700, CalculadoraNota.notaCentesimas(10001))
        assertEquals(700, CalculadoraNota.notaCentesimas(Int.MAX_VALUE))
    }

    @Test
    fun fraccionesDeCentesima_seTruncanSinAdelantarLaAprobacion() {
        assertEquals(100, CalculadoraNota.notaCentesimas(1))
        assertEquals(100, CalculadoraNota.notaCentesimas(19))
        assertEquals(399, CalculadoraNota.notaCentesimas(5999))
        assertEquals(400, CalculadoraNota.notaCentesimas(6001))
        assertEquals(400, CalculadoraNota.notaCentesimas(6013))
        assertEquals(414, CalculadoraNota.notaCentesimas(6199))
    }

    /** Interpolación decimal entre (0 %, 1), (60 %, 4) y (100 %, 7). */
    private fun notaReferencia(logro: Int): Int {
        val porcentaje = BigDecimal(logro).movePointLeft(2)
        val tramoInferior = porcentaje < BigDecimal("60")
        val inicio = if (tramoInferior) BigDecimal.ZERO else BigDecimal("60")
        val amplitud = if (tramoInferior) BigDecimal("60") else BigDecimal("40")
        val notaBase = if (tramoInferior) BigDecimal.ONE else BigDecimal("4")
        // Numerador exacto: no se divide ni se trunca ningún valor intermedio.
        val numerador = notaBase * amplitud + (porcentaje - inicio) * BigDecimal("3")
        return numerador.divide(amplitud, 2, RoundingMode.DOWN)
            .movePointRight(2).intValueExact()
    }
}
