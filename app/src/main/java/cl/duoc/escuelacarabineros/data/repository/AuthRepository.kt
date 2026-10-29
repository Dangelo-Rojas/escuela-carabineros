package cl.duoc.escuelacarabineros.data.repository

import cl.duoc.escuelacarabineros.data.local.dao.DocenteDao
import cl.duoc.escuelacarabineros.data.local.entity.DocenteEntity
import cl.duoc.escuelacarabineros.data.session.SessionManager
import cl.duoc.escuelacarabineros.model.RolDocente
import cl.duoc.escuelacarabineros.utils.Seguridad
import kotlinx.coroutines.flow.Flow

sealed interface ResultadoAuth {
    data class Exito(val docente: DocenteEntity) : ResultadoAuth
    data class Error(val mensaje: String) : ResultadoAuth
}

/**
 * Autenticación y gestión de cuentas.
 * Las cuentas NO se registran libremente: solo el Administrador las crea,
 * asigna su rol y puede desactivarlas o eliminarlas.
 */
class AuthRepository(
    private val docenteDao: DocenteDao,
    private val session: SessionManager
) {

    suspend fun login(correo: String, password: String): ResultadoAuth {
        val docente = docenteDao.buscarPorCorreo(correo.trim().lowercase())
            ?: return ResultadoAuth.Error("No existe una cuenta con ese correo")
        if (docente.passwordHash != Seguridad.hash(password)) {
            return ResultadoAuth.Error("Contraseña incorrecta")
        }
        if (!docente.activo) {
            return ResultadoAuth.Error("Tu cuenta está desactivada. Contacta al administrador.")
        }
        session.iniciar(docente.id, docente.nombre, docente.rol)
        return ResultadoAuth.Exito(docente)
    }

    fun cerrarSesion() = session.cerrar()

    // ---------------- Gestión de cuentas (solo Administrador) ----------------

    fun observarCuentas(): Flow<List<DocenteEntity>> = docenteDao.observarTodos()

    suspend fun obtenerCuenta(id: Long): DocenteEntity? = docenteDao.buscarPorId(id)

    suspend fun crearCuenta(nombre: String, correo: String, password: String, rol: RolDocente): ResultadoAuth {
        val correoNormalizado = correo.trim().lowercase()
        if (docenteDao.buscarPorCorreo(correoNormalizado) != null) {
            return ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        }
        val nueva = DocenteEntity(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            passwordHash = Seguridad.hash(password),
            rol = rol.etiqueta
        )
        val id = docenteDao.insertar(nueva)
        return ResultadoAuth.Exito(nueva.copy(id = id))
    }

    /**
     * Edita nombre, correo y rol. Si [nuevaPassword] viene vacía se mantiene la actual.
     */
    suspend fun actualizarCuenta(
        original: DocenteEntity,
        nombre: String,
        correo: String,
        rol: RolDocente,
        nuevaPassword: String
    ): ResultadoAuth {
        val correoNormalizado = correo.trim().lowercase()
        val otro = docenteDao.buscarPorCorreo(correoNormalizado)
        if (otro != null && otro.id != original.id) {
            return ResultadoAuth.Error("Ya existe otra cuenta con ese correo")
        }
        quitarAdministradorPermitido(original, rol, original.activo)?.let { return ResultadoAuth.Error(it) }

        val actualizada = original.copy(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            rol = rol.etiqueta,
            passwordHash = if (nuevaPassword.isBlank()) original.passwordHash else Seguridad.hash(nuevaPassword)
        )
        docenteDao.actualizar(actualizada)
        // Si el admin se editó a sí mismo, se refresca su nombre en la sesión
        if (original.id == session.docenteId.value) {
            session.iniciar(actualizada.id, actualizada.nombre, actualizada.rol)
        }
        return ResultadoAuth.Exito(actualizada)
    }

    /** Activa o desactiva el acceso de una cuenta (quitar acceso sin borrar su historial). */
    suspend fun cambiarActivo(cuenta: DocenteEntity, activo: Boolean): String? {
        if (cuenta.id == session.docenteId.value && !activo) return "No puedes desactivar tu propia cuenta"
        quitarAdministradorPermitido(cuenta, RolDocente.desdeEtiqueta(cuenta.rol), activo)?.let { return it }
        docenteDao.actualizar(cuenta.copy(activo = activo))
        return null
    }

    suspend fun eliminarCuenta(cuenta: DocenteEntity): String? {
        if (cuenta.id == session.docenteId.value) return "No puedes eliminar tu propia cuenta"
        quitarAdministradorPermitido(cuenta, RolDocente.DOCENTE, false)?.let { return it }
        docenteDao.eliminar(cuenta)
        return null
    }

    /**
     * Regla de negocio: siempre debe quedar al menos un administrador activo.
     * Devuelve un mensaje de error si el cambio dejaría la app sin administradores.
     */
    private suspend fun quitarAdministradorPermitido(
        cuenta: DocenteEntity,
        nuevoRol: RolDocente,
        nuevoActivo: Boolean
    ): String? {
        val eraAdminActivo = cuenta.rol == RolDocente.ADMINISTRADOR.etiqueta && cuenta.activo
        val seguiraAdminActivo = nuevoRol == RolDocente.ADMINISTRADOR && nuevoActivo
        if (eraAdminActivo && !seguiraAdminActivo && docenteDao.contarAdministradoresActivos() <= 1) {
            return "Debe quedar al menos un administrador activo"
        }
        return null
    }

    // ---------------- Datos ficticios iniciales ----------------

    /** Crea las cuentas de prueba la primera vez que se abre la app. Devuelve el id del docente demo. */
    suspend fun crearCuentasDemoSiNoExisten(): Long {
        if (docenteDao.buscarPorCorreo(CORREO_ADMIN) == null) {
            docenteDao.insertar(
                DocenteEntity(
                    nombre = "Administrador ESFOCAR",
                    correo = CORREO_ADMIN,
                    passwordHash = Seguridad.hash(PASSWORD_ADMIN),
                    rol = RolDocente.ADMINISTRADOR.etiqueta
                )
            )
        }
        docenteDao.buscarPorCorreo(CORREO_DEMO)?.let { return it.id }
        return docenteDao.insertar(
            DocenteEntity(
                nombre = "Docente Demo",
                correo = CORREO_DEMO,
                passwordHash = Seguridad.hash(PASSWORD_DEMO),
                rol = RolDocente.DOCENTE.etiqueta
            )
        )
    }

    companion object {
        const val CORREO_DEMO = "docente@esfocar.cl"
        const val PASSWORD_DEMO = "demo123"
        const val CORREO_ADMIN = "admin@esfocar.cl"
        const val PASSWORD_ADMIN = "admin123"
    }
}
