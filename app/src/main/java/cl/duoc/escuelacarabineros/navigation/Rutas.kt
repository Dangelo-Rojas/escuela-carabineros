package cl.duoc.escuelacarabineros.navigation

/** Rutas de navegación de la app. */
object Rutas {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CREAR_RUBRICA = "crear_rubrica"
    const val HISTORIAL = "historial"

    const val ARG_RUBRICA_ID = "rubricaId"
    const val EVALUAR = "evaluar/{$ARG_RUBRICA_ID}"
    fun evaluar(rubricaId: Long) = "evaluar/$rubricaId"

    const val EDITAR_RUBRICA = "editar_rubrica/{$ARG_RUBRICA_ID}"
    fun editarRubrica(rubricaId: Long) = "editar_rubrica/$rubricaId"

    // ---- Administración de cuentas (solo Administrador) ----
    const val CUENTAS = "cuentas"
    const val CUENTA_NUEVA = "cuenta_nueva"
    const val ARG_CUENTA_ID = "cuentaId"
    const val CUENTA_EDITAR = "cuenta_editar/{$ARG_CUENTA_ID}"
    fun editarCuenta(cuentaId: Long) = "cuenta_editar/$cuentaId"
}
