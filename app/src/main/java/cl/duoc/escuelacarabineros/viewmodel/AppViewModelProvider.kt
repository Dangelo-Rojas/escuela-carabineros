package cl.duoc.escuelacarabineros.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.escuelacarabineros.RubricasApp
import cl.duoc.escuelacarabineros.data.AppContainer

/** Fábrica única que entrega a cada ViewModel sus repositorios. */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { LoginViewModel(container().authRepository) }
        initializer {
            HomeViewModel(
                container().rubricaRepository,
                container().authRepository,
                container().session
            )
        }
        initializer {
            CrearRubricaViewModel(
                createSavedStateHandle(),
                container().rubricaRepository,
                container().session
            )
        }
        initializer {
            EvaluarViewModel(
                createSavedStateHandle(),
                container().rubricaRepository,
                container().evaluacionRepository,
                container().fotoStorage,
                container().session
            )
        }
        initializer { CuentasViewModel(container().authRepository, container().session) }
        initializer { CuentaFormViewModel(createSavedStateHandle(), container().authRepository) }
        initializer {
            HistorialViewModel(
                container().evaluacionRepository,
                container().pdfGenerator,
                container().session
            )
        }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[APPLICATION_KEY] as RubricasApp).container
}
