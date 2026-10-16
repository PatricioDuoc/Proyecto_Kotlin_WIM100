package com.example.appsemestral.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Candidatura al programa WIM 100. Solo datos ficticios.
 * El correo es único: evita registrar dos veces a la misma candidata.
 */
@Entity(
    tableName = "candidatas",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Candidata(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val area: String,
    val aniosExperiencia: Int,
    val resumenExperiencia: String,
    val linkedinUrl: String,
    val cvUri: String?,
    val cvNombre: String?,
    val estado: EstadoCandidatura = EstadoCandidatura.POSTULADA,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val registradaPor: String
) {
    companion object {
        val AREAS = listOf(
            "Seguridad ofensiva (Red Team)",
            "Seguridad defensiva (Blue Team)",
            "Gobierno, riesgo y cumplimiento",
            "Ciberseguridad industrial (OT)",
            "Forense y respuesta a incidentes",
            "Investigación y academia"
        )
    }
}
