package cl.duoc.escuelacarabineros.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Paleta provisoria basada en los verdes y el amarillo institucionales.
// Reemplazar por los códigos del manual de marca cuando la Escuela lo envíe.
val Verde10 = Color(0xFF002110)
val Verde20 = Color(0xFF00391D)
val Verde30 = Color(0xFF0E512E)
val Verde40 = Color(0xFF2B6A44)
val Verde80 = Color(0xFF8FD5A6)
val Verde90 = Color(0xFFAAF2C0)

val Amarillo20 = Color(0xFF3D2F00)
val Amarillo30 = Color(0xFF584400)
val Amarillo40 = Color(0xFF745B00)
val Amarillo80 = Color(0xFFF2C12E)
val Amarillo90 = Color(0xFFFFDF94)

val Gris10 = Color(0xFF191C1A)
val Gris90 = Color(0xFFE1E3DF)
val Gris95 = Color(0xFFF7FAF5)

val Rojo40 = Color(0xFFBA1A1A)
val Rojo80 = Color(0xFFFFB4AB)

// Colores semánticos para notas
val NotaAprobada: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val NotaReprobada: Color
    @Composable
    get() = MaterialTheme.colorScheme.error
