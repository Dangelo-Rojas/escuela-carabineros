package cl.duoc.escuelacarabineros.data

import android.content.Context
import cl.duoc.escuelacarabineros.data.local.AppDatabase
import cl.duoc.escuelacarabineros.data.repository.AuthRepository
import cl.duoc.escuelacarabineros.data.repository.EvaluacionRepository
import cl.duoc.escuelacarabineros.data.repository.RubricaRepository
import cl.duoc.escuelacarabineros.data.session.SessionManager
import cl.duoc.escuelacarabineros.utils.FotoStorage
import cl.duoc.escuelacarabineros.utils.PdfGenerator

/** Contenedor de dependencias: crea una sola instancia de cada repositorio. */
class AppContainer(context: Context) {

    private val db = AppDatabase.obtener(context)

    val session = SessionManager(context)
    val fotoStorage = FotoStorage(context)
    val pdfGenerator = PdfGenerator(context)

    val authRepository = AuthRepository(db.docenteDao(), session)
    val rubricaRepository = RubricaRepository(db.rubricaDao())
    val evaluacionRepository = EvaluacionRepository(db.evaluacionDao(), fotoStorage)
}
