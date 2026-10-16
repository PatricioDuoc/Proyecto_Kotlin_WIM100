package com.example.appsemestral.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evaluación de una candidata realizada por una evaluadora.
 * El índice único (candidataId, evaluadoraClave) impide evaluaciones duplicadas
 * incluso a nivel de base de datos.
 */
@Entity(
    tableName = "evaluaciones",
    foreignKeys = [
        ForeignKey(
            entity = Candidata::class,
            parentColumns = ["id"],
            childColumns = ["candidataId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["candidataId", "evaluadoraClave"], unique = true)]
)
data class Evaluacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val candidataId: Long,
    val evaluadora: String,
    val evaluadoraClave: String,
    val puntajeTotal: Double,
    val observacionGeneral: String,
    val fecha: Long = System.currentTimeMillis()
) {
    companion object {
        /** Evaluaciones mínimas para considerar una candidatura "Evaluada" (consolidada). */
        const val MINIMO_PARA_CONSOLIDAR = 2

        /** "  Ana   Pérez " y "ana pérez" se consideran la misma evaluadora. */
        fun normalizarClave(nombre: String): String =
            nombre.trim().lowercase().replace(Regex("\\s+"), " ")
    }
}
