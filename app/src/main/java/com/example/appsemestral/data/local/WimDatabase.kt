package com.example.appsemestral.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.appsemestral.data.local.dao.CandidataDao
import com.example.appsemestral.data.local.dao.CriterioDao
import com.example.appsemestral.data.local.dao.EvaluacionDao
import com.example.appsemestral.data.local.dao.HistorialDao
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.Criterio
import com.example.appsemestral.model.Evaluacion
import com.example.appsemestral.model.PuntajeCriterio
import com.example.appsemestral.model.RegistroHistorial

/** Base de datos local SQLite gestionada con Room (archivo wim100.db). */
@Database(
    entities = [
        Candidata::class,
        Criterio::class,
        Evaluacion::class,
        PuntajeCriterio::class,
        RegistroHistorial::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WimDatabase : RoomDatabase() {

    abstract fun candidataDao(): CandidataDao
    abstract fun criterioDao(): CriterioDao
    abstract fun evaluacionDao(): EvaluacionDao
    abstract fun historialDao(): HistorialDao

    companion object {
        @Volatile
        private var instancia: WimDatabase? = null

        /** Singleton: una sola conexión a la BD para toda la app. */
        fun obtener(context: Context): WimDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    WimDatabase::class.java,
                    "wim100.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
