package cl.duoc.escuelacarabineros

import cl.duoc.escuelacarabineros.model.CalculadoraNota
import cl.duoc.escuelacarabineros.model.NivelLogro
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
}
