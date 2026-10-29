package cl.duoc.escuelacarabineros.utils

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Guarda las fotos de evidencia en el almacenamiento interno de la app. */
class FotoStorage(private val context: Context) {

    suspend fun guardar(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val archivo = File(carpeta, "evidencia_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { salida ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, salida)
        }
        archivo.absolutePath
    }

    fun eliminar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }
}
