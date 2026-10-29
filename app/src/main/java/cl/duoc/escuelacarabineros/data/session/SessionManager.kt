package cl.duoc.escuelacarabineros.data.session

import android.content.Context
import androidx.core.content.edit
import cl.duoc.escuelacarabineros.model.RolDocente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda la sesión del docente en SharedPreferences (almacenamiento local). */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    private val _docenteId = MutableStateFlow(prefs.getLong(KEY_ID, SIN_SESION))
    val docenteId: StateFlow<Long> = _docenteId.asStateFlow()

    val nombreDocente: String
        get() = prefs.getString(KEY_NOMBRE, "") ?: ""

    val rol: String
        get() = prefs.getString(KEY_ROL, "") ?: ""

    /** Solo el administrador ve y usa la gestión de cuentas. */
    val esAdministrador: Boolean
        get() = rol == RolDocente.ADMINISTRADOR.etiqueta

    val haySesion: Boolean
        get() = _docenteId.value != SIN_SESION

    /** Última sede usada, para no tener que elegirla en cada evaluación. */
    var ultimaSede: String
        get() = prefs.getString(KEY_SEDE, "") ?: ""
        set(valor) = prefs.edit { putString(KEY_SEDE, valor) }

    fun iniciar(id: Long, nombre: String, rol: String) {
        prefs.edit {
            putLong(KEY_ID, id)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_ROL, rol)
        }
        _docenteId.value = id
    }

    fun cerrar() {
        prefs.edit {
            remove(KEY_ID)
            remove(KEY_NOMBRE)
            remove(KEY_ROL)
        }
        _docenteId.value = SIN_SESION
    }

    companion object {
        const val SIN_SESION = -1L
        private const val KEY_ID = "docente_id"
        private const val KEY_NOMBRE = "docente_nombre"
        private const val KEY_SEDE = "ultima_sede"
        private const val KEY_ROL = "docente_rol"
    }
}
