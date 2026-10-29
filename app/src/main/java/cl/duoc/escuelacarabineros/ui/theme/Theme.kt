package cl.duoc.escuelacarabineros.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = Verde40,
    onPrimary = Gris95,
    primaryContainer = Verde90,
    onPrimaryContainer = Verde10,
    secondary = Amarillo40,
    onSecondary = Gris95,
    secondaryContainer = Amarillo90,
    onSecondaryContainer = Amarillo20,
    background = Gris95,
    onBackground = Gris10,
    surface = Gris95,
    onSurface = Gris10,
    error = Rojo40
)

private val EsquemaOscuro = darkColorScheme(
    primary = Verde80,
    onPrimary = Verde20,
    primaryContainer = Verde30,
    onPrimaryContainer = Verde90,
    secondary = Amarillo80,
    onSecondary = Amarillo20,
    secondaryContainer = Amarillo30,
    onSecondaryContainer = Amarillo90,
    background = Gris10,
    onBackground = Gris90,
    surface = Gris10,
    onSurface = Gris90,
    error = Rojo80
)

/**
 * Tema Material 3 de la app. No usa colores dinámicos del sistema
 * para mantener siempre la identidad institucional.
 */
@Composable
fun RubricasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
