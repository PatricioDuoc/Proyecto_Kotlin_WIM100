package com.example.appsemestral.nativo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File

/** Estado de la lectura de un PDF para el visor integrado. */
sealed interface ResultadoPdf {
    data object Cargando : ResultadoPdf
    data class Ok(val paginas: List<Bitmap>, val totalPaginas: Int) : ResultadoPdf
    data class Error(val mensaje: String) : ResultadoPdf
}

data class InfoArchivo(val nombre: String?, val tamano: Long?)

/**
 * RECURSO NATIVO: selector de documentos (Storage Access Framework) + lectura de PDF.
 * El SAF no requiere permisos de almacenamiento: la usuaria elige el archivo y la app
 * recibe acceso solo a ese archivo (se persiste el permiso de lectura).
 */
object ArchivoHelper {

    fun leerInfo(context: Context, uri: Uri): InfoArchivo {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val iNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val iTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                val nombre = if (iNombre >= 0) cursor.getString(iNombre) else null
                val tamano = if (iTamano >= 0 && !cursor.isNull(iTamano)) cursor.getLong(iTamano) else null
                return InfoArchivo(nombre, tamano)
            }
        }
        return InfoArchivo(uri.lastPathSegment, null)
    }

    /** Mantiene el acceso al archivo aunque se reinicie la app. */
    fun persistirPermiso(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Algunos proveedores no permiten persistir: el acceso dura mientras viva la app
        }
    }

    /** Renderiza las primeras páginas del PDF como imágenes (visor integrado, sin apps externas). */
    fun renderizarPdf(context: Context, uriTexto: String?, maxPaginas: Int = 5, ancho: Int = 1080): ResultadoPdf {
        if (uriTexto.isNullOrBlank()) return ResultadoPdf.Error("Esta candidatura no tiene CV adjunto")
        return try {
            val descriptor = context.contentResolver.openFileDescriptor(Uri.parse(uriTexto), "r")
                ?: return ResultadoPdf.Error("No se pudo abrir el archivo")
            descriptor.use { pfd ->
                val renderer = PdfRenderer(pfd)
                try {
                    val total = renderer.pageCount
                    val paginas = (0 until minOf(total, maxPaginas)).map { i ->
                        val pagina = renderer.openPage(i)
                        try {
                            val alto = (ancho.toFloat() / pagina.width * pagina.height).toInt()
                            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmap
                        } finally {
                            pagina.close()
                        }
                    }
                    ResultadoPdf.Ok(paginas, total)
                } finally {
                    renderer.close()
                }
            }
        } catch (e: SecurityException) {
            ResultadoPdf.Error("Se perdió el permiso de lectura. Vuelve a adjuntar el CV.")
        } catch (e: Exception) {
            ResultadoPdf.Error("No se pudo leer el PDF (${e.javaClass.simpleName})")
        }
    }

    /**
     * Genera un CV de prueba en PDF con la API nativa PdfDocument (datos ficticios)
     * y lo expone mediante FileProvider (content://).
     */
    fun generarCvDemo(
        context: Context,
        nombre: String,
        correo: String,
        area: String,
        anios: Int,
        resumen: String
    ): Pair<Uri, String> {
        val carpeta = File(context.filesDir, "cvs").apply { mkdirs() }
        val nombreArchivo = "CV_demo_" + nombre.lowercase()
            .replace(Regex("[^a-z ]"), "")
            .trim()
            .replace(" ", "_") + ".pdf"
        val archivo = File(carpeta, nombreArchivo)

        val documento = PdfDocument()
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = pagina.canvas
        val titulo = Paint().apply { textSize = 22f; typeface = Typeface.DEFAULT_BOLD; color = Color.rgb(74, 20, 140) }
        val subtitulo = Paint().apply { textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = Color.DKGRAY }
        val texto = Paint().apply { textSize = 12f; color = Color.BLACK }
        val aviso = Paint().apply { textSize = 10f; color = Color.RED }

        var y = 60f
        canvas.drawText(nombre, 50f, y, titulo); y += 22f
        canvas.drawText("$area · $anios años de experiencia", 50f, y, texto); y += 18f
        canvas.drawText(correo, 50f, y, texto); y += 30f
        canvas.drawText("PERFIL PROFESIONAL", 50f, y, subtitulo); y += 20f
        for (linea in dividirEnLineas(resumen, 80)) {
            canvas.drawText(linea, 50f, y, texto); y += 16f
        }
        y += 14f
        canvas.drawText("FORMACIÓN", 50f, y, subtitulo); y += 20f
        canvas.drawText("Ingeniería en Informática (institución ficticia)", 50f, y, texto); y += 16f
        canvas.drawText("Diplomado en Ciberseguridad (institución ficticia)", 50f, y, texto)
        canvas.drawText("DOCUMENTO DE PRUEBA · DATOS SINTÉTICOS · MVP ACADÉMICO WIM 100", 50f, 800f, aviso)
        documento.finishPage(pagina)
        archivo.outputStream().use { documento.writeTo(it) }
        documento.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        return uri to nombreArchivo
    }

    private fun dividirEnLineas(texto: String, maxCaracteres: Int): List<String> {
        val lineas = mutableListOf<String>()
        var actual = StringBuilder()
        for (palabra in texto.split(" ")) {
            if (actual.length + palabra.length + 1 > maxCaracteres) {
                lineas.add(actual.toString())
                actual = StringBuilder()
            }
            if (actual.isNotEmpty()) actual.append(' ')
            actual.append(palabra)
        }
        if (actual.isNotEmpty()) lineas.add(actual.toString())
        return lineas
    }
}
