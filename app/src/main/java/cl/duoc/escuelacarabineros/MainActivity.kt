package cl.duoc.escuelacarabineros

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.duoc.escuelacarabineros.navigation.AppNavigation
import cl.duoc.escuelacarabineros.ui.theme.RubricasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val haySesion =
            (application as RubricasApp).container.session.haySesion

        val preferencias =
            getSharedPreferences("apariencia", MODE_PRIVATE)

        setContent {
            var modoTema by remember {
                mutableStateOf(
                    preferencias.getString("modo_tema", "sistema")
                        ?: "sistema"
                )
            }

            val oscuroSistema = isSystemInDarkTheme()

            val oscuro = when (modoTema) {
                "claro" -> false
                "oscuro" -> true
                else -> oscuroSistema
            }

            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    ) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(
                        0xE6FFFFFF.toInt(),
                        0x801B1B1B.toInt()
                    ) { oscuro }
                )
            }

            RubricasTheme(darkTheme = oscuro) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        haySesion = haySesion,
                        modoTema = modoTema,
                        onCambiarTema = { nuevoModo ->
                            preferencias.edit()
                                .putString("modo_tema", nuevoModo)
                                .apply()

                            modoTema = nuevoModo
                        }
                    )
                }
            }
        }
    }
}
