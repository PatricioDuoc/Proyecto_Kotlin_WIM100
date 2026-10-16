package com.example.appsemestral.di

import android.content.Context
import com.example.appsemestral.data.local.WimDatabase
import com.example.appsemestral.nativo.Notificador
import com.example.appsemestral.nativo.NotificadorAndroid
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.data.repository.EvaluacionRepository
import com.example.appsemestral.data.repository.HistorialRepository

/** Inyección de dependencias manual: crea una sola instancia de cada repositorio. */
class AppContainer(context: Context) {
    private val db = WimDatabase.obtener(context)

    val historialRepository = HistorialRepository(db.historialDao())
    val candidataRepository = CandidataRepository(db.candidataDao(), historialRepository)
    val evaluacionRepository = EvaluacionRepository(db, historialRepository)
    val notificador: Notificador = NotificadorAndroid(context.applicationContext)
}
