package cl.duoc.escuelacarabineros.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Obtiene la ubicación GPS del dispositivo con LocationManager (API nativa de Android).
 * Debe llamarse SOLO después de que el usuario concedió el permiso de ubicación.
 */
object UbicacionHelper {

    fun gpsActivado(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission") // el permiso se verifica en la pantalla antes de llamar
    suspend fun obtenerUbicacion(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val proveedor = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }

        // 1) Intenta una ubicación nueva (máx. 10 segundos)
        val actual = withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    lm.getCurrentLocation(
                        proveedor,
                        null,
                        ContextCompat.getMainExecutor(context)
                    ) { location -> if (cont.isActive) cont.resume(location) }
                } else {
                    @Suppress("DEPRECATION")
                    lm.requestSingleUpdate(
                        proveedor,
                        { location -> if (cont.isActive) cont.resume(location) },
                        Looper.getMainLooper()
                    )
                }
            }
        }
        // 2) Si no llega, usa la última conocida
        return actual
            ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }
}
