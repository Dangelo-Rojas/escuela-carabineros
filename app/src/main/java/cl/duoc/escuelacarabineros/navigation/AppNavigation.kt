package cl.duoc.escuelacarabineros.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.escuelacarabineros.ui.screens.CrearRubricaScreen
import cl.duoc.escuelacarabineros.ui.screens.EvaluarScreen
import cl.duoc.escuelacarabineros.ui.screens.HistorialScreen
import cl.duoc.escuelacarabineros.ui.screens.HomeScreen
import cl.duoc.escuelacarabineros.ui.screens.LoginScreen
import cl.duoc.escuelacarabineros.ui.screens.CuentaFormScreen
import cl.duoc.escuelacarabineros.ui.screens.CuentasScreen

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
    NavHost(
        navController = navController,
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
                modoTema = modoTema,
                onCambiarTema = onCambiarTema,
                onCrearRubrica = { navController.navigate(Rutas.CREAR_RUBRICA) },
                onEvaluar = { id -> navController.navigate(Rutas.evaluar(id)) },
                onEditar = { id -> navController.navigate(Rutas.editarRubrica(id)) },
                onHistorial = { navController.navigate(Rutas.HISTORIAL) },
                onAdministrarCuentas = { navController.navigate(Rutas.CUENTAS) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
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
                    }
                }
            )
        }

        // ---- Administración de cuentas ----
        composable(Rutas.CUENTAS) {
            CuentasScreen(
                onVolver = { navController.popBackStack() },
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
            HistorialScreen(onVolver = { navController.popBackStack() })
        }
    }
}
