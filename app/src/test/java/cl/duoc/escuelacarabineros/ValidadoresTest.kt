package cl.duoc.escuelacarabineros

import cl.duoc.escuelacarabineros.utils.Validadores
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    @Test
    fun correo() {
        assertNotNull(Validadores.correo(""))
        assertNotNull(Validadores.correo("dangelo"))
        assertNull(Validadores.correo("docente@esfocar.cl"))
    }

    @Test
    fun passwordNueva() {
        assertNotNull(Validadores.passwordNueva("abc"))
        assertNotNull(Validadores.passwordNueva("abcdef"))
        assertNull(Validadores.passwordNueva("abc123"))
    }

    @Test
    fun confirmarPassword() {
        assertNotNull(Validadores.confirmarPassword("abc123", "abc124"))
        assertNull(Validadores.confirmarPassword("abc123", "abc123"))
    }

    @Test
    fun ponderacion() {
        assertNotNull(Validadores.ponderacion(""))
        assertNotNull(Validadores.ponderacion("abc"))
        assertNotNull(Validadores.ponderacion("0"))
        assertNotNull(Validadores.ponderacion("101"))
        assertNull(Validadores.ponderacion("50"))
    }

    @Test
    fun sumaPonderaciones() {
        assertNull(Validadores.sumaPonderaciones(100))
        assertNotNull(Validadores.sumaPonderaciones(90))
    }

    @Test
    fun nombrePersona() {
        assertNull(Validadores.nombrePersona("Juan Pérez"))
        assertNotNull(Validadores.nombrePersona("Jo"))
        assertNotNull(Validadores.nombrePersona("Juan2"))
    }
}
