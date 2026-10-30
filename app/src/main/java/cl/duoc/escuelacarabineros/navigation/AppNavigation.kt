package cl.duoc.escuelacarabineros.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.escuelacarabineros.ui.screens.CrearRubricaScreen
import cl.duoc.escuelacarabineros.ui.screens.EvaluarScreen
import cl.duoc.escuelacarabineros.ui.screens.HistorialScreen
import cl.duoc.escuelacarabineros.ui.screens.HomeScreen
import cl.duoc.escuelacarabineros.ui.screens.LoginScreen
import cl.duoc.escuelacarabineros.ui.screens.CuentaFormScreen
import cl.duoc.escuelacarabineros.ui.screens.CuentasScreen
import cl.duoc.escuelacarabineros.ui.components.NavegacionPrincipal
import cl.duoc.escuelacarabineros.viewmodel.AppViewModelProvider
import cl.duoc.escuelacarabineros.viewmodel.HomeViewModel

private const val DURACION = 350

/**
 * Grafo de navegación. Las transiciones deslizan las pantallas
 * (animación entre vistas para dar fluidez).
 */
@Composable
fun AppNavigation(
    haySesion: Boolean,
    modoTema: String,
    onCambiarTema: (String) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val secciones = listOf(Rutas.HOME, Rutas.HISTORIAL, Rutas.CUENTAS)
    // La entrada Home sigue siendo la raíz de la sesión y conserva su ViewModel.
    val modeloPrincipal: HomeViewModel? = if (rutaActual in secciones) {
        val entradaHome = remember(entradaActual) { navController.getBackStackEntry(Rutas.HOME) }
        viewModel(viewModelStoreOwner = entradaHome, factory = AppViewModelProvider.Factory)
    } else null

    fun navegarSeccion(ruta: String) {
        // Consultar el controlador al pulsar, no una ruta capturada por un callback anterior.
        if (ruta == navController.currentDestination?.route) return
        navController.navigate(ruta) {
            popUpTo(Rutas.HOME) { saveState = true }
            launchSingleTop = true
            // Home permanece en la pila; restaurarlo recuperaría la pestaña recién guardada.
            restoreState = ruta != Rutas.HOME
        }
    }

    NavegacionPrincipal(
        rutaActual = rutaActual,
        nombreUsuario = modeloPrincipal?.nombreDocente.orEmpty(),
        esAdministrador = modeloPrincipal?.esAdministrador == true,
        modoTema = modoTema,
        onCambiarTema = onCambiarTema,
        onSeccion = { ruta -> navegarSeccion(ruta) },
        onCerrarSesion = {
            modeloPrincipal?.cerrarSesion()
            // También descarta los estados guardados de otras pestañas de esta sesión.
            secciones.forEach { navController.clearBackStack(it) }
            navController.navigate(Rutas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    ) { modifier ->
        NavHost(
            navController = navController,
            modifier = modifier.fillMaxSize(),
            startDestination = if (haySesion) Rutas.HOME else Rutas.LOGIN,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION)) +
                    fadeIn(tween(DURACION))
            },
            exitTransition = { fadeOut(tween(DURACION)) },
            popEnterTransition = { fadeIn(tween(DURACION)) },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION)) +
                    fadeOut(tween(DURACION))
            }
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    onLoginExitoso = {
                        navController.navigate(Rutas.HOME) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Rutas.HOME) {
                HomeScreen(
                    onCrearRubrica = { navController.navigate(Rutas.CREAR_RUBRICA) },
                    onEvaluar = { id -> navController.navigate(Rutas.evaluar(id)) },
                    onEditar = { id -> navController.navigate(Rutas.editarRubrica(id)) }
                )
            }

            composable(Rutas.CREAR_RUBRICA) {
                CrearRubricaScreen(onVolver = { navController.popBackStack() })
            }

            // Misma pantalla del formulario, pero cargando la rúbrica a editar
            composable(
                route = Rutas.EDITAR_RUBRICA,
                arguments = listOf(navArgument(Rutas.ARG_RUBRICA_ID) { type = NavType.LongType })
            ) {
                CrearRubricaScreen(onVolver = { navController.popBackStack() })
            }

            composable(
                route = Rutas.EVALUAR,
                arguments = listOf(navArgument(Rutas.ARG_RUBRICA_ID) { type = NavType.LongType })
            ) {
                EvaluarScreen(
                    onVolver = { navController.popBackStack() },
                    onGuardado = {
                        navController.navigate(Rutas.HISTORIAL) {
                            popUpTo(Rutas.HOME)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ---- Administración de cuentas ----
            composable(Rutas.CUENTAS) {
                CuentasScreen(
                    onCrearCuenta = { navController.navigate(Rutas.CUENTA_NUEVA) },
                    onEditarCuenta = { id -> navController.navigate(Rutas.editarCuenta(id)) }
                )
            }

            composable(Rutas.CUENTA_NUEVA) {
                CuentaFormScreen(onVolver = { navController.popBackStack() })
            }

            composable(
                route = Rutas.CUENTA_EDITAR,
                arguments = listOf(navArgument(Rutas.ARG_CUENTA_ID) { type = NavType.LongType })
            ) {
                CuentaFormScreen(onVolver = { navController.popBackStack() })
            }

            composable(Rutas.HISTORIAL) {
                HistorialScreen()
            }
        }
    }
}
