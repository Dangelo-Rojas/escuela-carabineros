package cl.duoc.escuelacarabineros.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import cl.duoc.escuelacarabineros.data.local.entity.EvaluacionConRubrica
import cl.duoc.escuelacarabineros.model.CalculadoraNota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Genera un PDF con el resultado de una evaluación usando PdfDocument (API nativa de Android),
 * para que el docente pueda compartirlo con el alumno.
 */
class PdfGenerator(private val context: Context) {

    private val anchoPagina = 595 // A4 en puntos
    private val altoPagina = 842
    private val margen = 40f
    private val anchoContenido = (anchoPagina - 2 * margen).toInt()

    private val verde = Color.rgb(43, 106, 68)
    private val rojo = Color.rgb(186, 26, 26)

    suspend fun generar(item: EvaluacionConRubrica, nombreDocente: String): File =
        withContext(Dispatchers.IO) {
            val ev = item.evaluacion
            val documento = PdfDocument()
            val escritor = Escritor(documento)

            // ---------- Encabezado ----------
            escritor.franjaTitulo("Resultado de evaluación")
            escritor.texto(item.nombreRubrica, 16f, negrita = true)
            escritor.espacio(6f)

            val fecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(ev.fecha))
            escritor.parClave("Evaluado", "${ev.evaluado} (${ev.tipo})")
            if (ev.sede.isNotBlank()) escritor.parClave("Sede", ev.sede)
            escritor.parClave("Fecha", fecha)
            escritor.parClave("Docente / Instructor", nombreDocente.ifBlank { "—" })
            escritor.espacio(10f)

            // ---------- Nota ----------
            val aprobada = CalculadoraNota.esAprobada(ev.notaCentesimas)
            escritor.texto(
                "Nota: ${CalculadoraNota.formatear(ev.notaCentesimas)}",
                26f,
                negrita = true,
                color = if (aprobada) verde else rojo
            )
            escritor.texto("Porcentaje de logro: ${ev.porcentajeLogro} %", 12f)
            escritor.espacio(12f)

            // ---------- Detalle por criterio ----------
            escritor.texto("Detalle por criterio", 14f, negrita = true)
            escritor.linea()
            ev.detalle.lines().filter { it.isNotBlank() }.forEach { linea ->
                val partes = linea.split("|")
                escritor.filaCriterio(partes.getOrElse(0) { "" }, partes.getOrElse(1) { "" })
            }
            escritor.espacio(12f)

            // ---------- Comentario ----------
            if (ev.comentario.isNotBlank()) {
                escritor.texto("Comentario del docente", 14f, negrita = true)
                escritor.texto(ev.comentario, 12f)
                escritor.espacio(12f)
            }

            // ---------- Evidencias ----------
            ev.fotoPath?.let { ruta ->
                BitmapFactory.decodeFile(ruta)?.let { foto ->
                    escritor.texto("Foto de evidencia", 14f, negrita = true)
                    escritor.imagen(foto)
                }
            }
            if (ev.latitud != null && ev.longitud != null) {
                escritor.parClave("Ubicación", "%.5f, %.5f".format(ev.latitud, ev.longitud))
            }

            escritor.espacio(20f)
            escritor.texto(
                "Documento generado con la app Escuela de Carabineros (prototipo académico, datos ficticios).",
                9f,
                color = Color.GRAY
            )
            escritor.terminar()

            val carpeta = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val nombreArchivo = "evaluacion_${ev.evaluado.replace(Regex("[^A-Za-z0-9]"), "_")}_${ev.id}.pdf"
            val archivo = File(carpeta, nombreArchivo)
            FileOutputStream(archivo).use { documento.writeTo(it) }
            documento.close()
            archivo
        }

    /** Ayudante que escribe de arriba hacia abajo y crea páginas nuevas cuando se acaba el espacio. */
    private inner class Escritor(private val documento: PdfDocument) {
        private var numeroPagina = 0
        private lateinit var pagina: PdfDocument.Page
        private lateinit var canvas: Canvas
        private var y = 0f

        init { nuevaPagina() }

        private fun nuevaPagina() {
            if (numeroPagina > 0) documento.finishPage(pagina)
            numeroPagina++
            pagina = documento.startPage(
                PdfDocument.PageInfo.Builder(anchoPagina, altoPagina, numeroPagina).create()
            )
            canvas = pagina.canvas
            y = margen
        }

        private fun asegurarEspacio(alto: Float) {
            if (y + alto > altoPagina - margen) nuevaPagina()
        }

        fun espacio(alto: Float) { y += alto }

        fun franjaTitulo(titulo: String) {
            val paint = Paint().apply { color = verde }
            canvas.drawRect(0f, 0f, anchoPagina.toFloat(), 70f, paint)
            val textoPaint = TextPaint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText(titulo, margen, 44f, textoPaint)
            y = 95f
        }

        fun texto(
            contenido: String,
            tamano: Float,
            negrita: Boolean = false,
            color: Int = Color.BLACK,
            ancho: Int = anchoContenido,
            x: Float = margen
        ) {
            val paint = TextPaint().apply {
                this.color = color
                textSize = tamano
                typeface = if (negrita) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                isAntiAlias = true
            }
            val layout = StaticLayout.Builder
                .obtain(contenido, 0, contenido.length, paint, ancho)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()
            asegurarEspacio(layout.height.toFloat())
            canvas.save()
            canvas.translate(x, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + 4f
        }

        fun parClave(clave: String, valor: String) = texto("$clave: $valor", 12f)

        fun linea() {
            val paint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
            canvas.drawLine(margen, y, anchoPagina - margen, y, paint)
            y += 6f
        }

        fun filaCriterio(criterio: String, nivel: String) {
            val yInicio = y
            val anchoNivel = 120
            texto(criterio, 11f, ancho = anchoContenido - anchoNivel - 10)
            val yFin = y
            y = yInicio
            texto(nivel, 11f, negrita = true, color = verde, ancho = anchoNivel, x = anchoPagina - margen - anchoNivel)
            y = maxOf(yFin, y) + 2f
            linea()
        }

        fun imagen(bitmap: android.graphics.Bitmap) {
            val anchoMax = 260f
            val escala = anchoMax / bitmap.width
            val alto = bitmap.height * escala
            asegurarEspacio(alto)
            canvas.drawBitmap(bitmap, null, RectF(margen, y, margen + anchoMax, y + alto), null)
            y += alto + 8f
        }

        fun terminar() = documento.finishPage(pagina)
    }
}
