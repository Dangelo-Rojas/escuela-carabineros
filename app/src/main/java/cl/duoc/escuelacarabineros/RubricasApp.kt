package cl.duoc.escuelacarabineros

import android.app.Application
import cl.duoc.escuelacarabineros.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RubricasApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Sesiones de versiones anteriores (sin rol guardado) se cierran por seguridad
        if (container.session.haySesion && container.session.rol.isBlank()) {
            container.session.cerrar()
        }

        // Datos ficticios iniciales (cuenta demo + una rúbrica de ejemplo)
        appScope.launch {
            val docenteId = container.authRepository.crearCuentasDemoSiNoExisten()
            container.rubricaRepository.crearRubricaDemoSiVacia(docenteId)
        }
    }
}
