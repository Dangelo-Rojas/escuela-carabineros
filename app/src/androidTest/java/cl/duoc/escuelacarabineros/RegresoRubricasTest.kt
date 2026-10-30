package cl.duoc.escuelacarabineros

import android.os.SystemClock
import android.graphics.Rect
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.repository.ResultadoAuth
import cl.duoc.escuelacarabineros.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Recorre la interfaz real mediante accesibilidad, sin depender de Espresso. */
@RunWith(AndroidJUnit4::class)
class RegresoRubricasTest {
    private val instrumentacion = InstrumentationRegistry.getInstrumentation()
    private lateinit var escenario: ActivityScenario<MainActivity>
    private lateinit var sesion: SessionManager
    private var idOriginal = SessionManager.SIN_SESION
    private var nombreOriginal = ""
    private var rolOriginal = ""

    @Before
    fun iniciarAdministrador() {
        val app = instrumentacion.targetContext.applicationContext as RubricasApp
        sesion = app.container.session
        idOriginal = sesion.docenteId.value
        nombreOriginal = sesion.nombreDocente
        rolOriginal = sesion.rol
        val resultado = runBlocking {
            // La aplicación crea las cuentas demo en segundo plano; no duplicar esa escritura.
            withTimeout(5_000) {
                var autenticacion = app.container.authRepository.login(AuthRepository.CORREO_ADMIN, AuthRepository.PASSWORD_ADMIN)
                while (autenticacion !is ResultadoAuth.Exito) {
                    delay(100)
                    autenticacion = app.container.authRepository.login(AuthRepository.CORREO_ADMIN, AuthRepository.PASSWORD_ADMIN)
                }
                autenticacion
            }
        }
        assertTrue("La cuenta demo debe iniciar sesión", resultado is ResultadoAuth.Exito)
        escenario = ActivityScenario.launch(MainActivity::class.java)
        esperarSeleccion("Rúbricas")
    }

    @After
    fun restaurarSesion() {
        if (::escenario.isInitialized) escenario.close()
        if (::sesion.isInitialized) {
            if (idOriginal == SessionManager.SIN_SESION) sesion.cerrar()
            else sesion.iniciar(idOriginal, nombreOriginal, rolOriginal)
        }
    }

    @Test
    fun desdeHistorial_pulsarRubricas_abreRubricas() {
        pulsarYComprobar("Historial")
        pulsarYComprobar("Rúbricas")
    }

    @Test
    fun desdeCuentas_pulsarRubricas_abreRubricas() {
        pulsarYComprobar("Cuentas")
        pulsarYComprobar("Rúbricas")
    }

    @Test
    fun alternarLasTresPestanas_siempreSeleccionaElDestinoPulsado() {
        listOf("Historial", "Cuentas", "Rúbricas", "Cuentas", "Historial", "Rúbricas")
            .forEach(::pulsarYComprobar)
    }

    @Test
    fun despuesDeLogin_puedeVolverARubricasDesdeAmbasSecciones() {
        escenario.close()
        sesion.cerrar()
        escenario = ActivityScenario.launch(MainActivity::class.java)
        instrumentacion.uiAutomation.waitForIdle(500, 3_000)
        pulsar("Demo admin")
        pulsar("Iniciar sesión")
        esperarSeleccion("Rúbricas")
        listOf("Historial", "Rúbricas", "Cuentas", "Rúbricas").forEach(::pulsarYComprobar)
    }

    private fun pulsarYComprobar(titulo: String) {
        pulsar(titulo)
        esperarSeleccion(titulo)
    }

    private fun pulsar(titulo: String) {
        val nodo = nodos(instrumentacion.uiAutomation.rootInActiveWindow)
            .firstOrNull { it.text?.toString() == titulo }
        requireNotNull(nodo) { "Debe existir el acceso $titulo" }
        val limites = Rect().also(nodo::getBoundsInScreen)
        val inicio = SystemClock.uptimeMillis()
        val bajar = MotionEvent.obtain(inicio, inicio, MotionEvent.ACTION_DOWN,
            limites.exactCenterX(), limites.exactCenterY(), 0)
        bajar.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            assertTrue(instrumentacion.uiAutomation.injectInputEvent(bajar, true))
            SystemClock.sleep(50)
            val subir = MotionEvent.obtain(inicio, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
                limites.exactCenterX(), limites.exactCenterY(), 0)
            subir.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                assertTrue(instrumentacion.uiAutomation.injectInputEvent(subir, true))
            } finally {
                subir.recycle()
            }
        } finally {
            bajar.recycle()
        }
    }

    private fun esperarSeleccion(titulo: String) {
        val limite = SystemClock.uptimeMillis() + 5_000
        do {
            val seleccionada = nodos(instrumentacion.uiAutomation.rootInActiveWindow)
                .filter { it.isSelected }.any { pestaña ->
                    nodos(pestaña).any { it.text?.toString() == titulo }
            }
            if (seleccionada) {
                // Esperar también el final de las transiciones antes del siguiente clic.
                instrumentacion.uiAutomation.waitForIdle(500, 3_000)
                return
            }
            SystemClock.sleep(100)
        } while (SystemClock.uptimeMillis() < limite)
        val visibles = nodos(instrumentacion.uiAutomation.rootInActiveWindow)
            .filter { it.text?.isNotBlank() == true || it.isSelected }
            .map {
                "${it.text} (seleccionado=${it.isSelected}, hijos=${if (it.isSelected) nodos(it).map { hijo -> hijo.text }.toList() else emptyList<String>()})"
            }.toList()
        throw AssertionError("Tras pulsar $titulo, esa pestaña debe quedar seleccionada. Árbol: $visibles")
    }

    private fun nodos(nodo: AccessibilityNodeInfo?): Sequence<AccessibilityNodeInfo> = sequence {
        // Navigation reutiliza nodos al restaurar pestañas; renovar la caché de accesibilidad.
        if (nodo != null && nodo.refresh()) {
            yield(nodo)
            for (indice in 0 until nodo.childCount) yieldAll(nodos(nodo.getChild(indice)))
        }
    }
}
