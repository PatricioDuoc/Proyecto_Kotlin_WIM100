package com.example.appsemestral.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Criterio de la matriz WIM. El peso es un porcentaje (la suma de todos debe ser 100).
 * La matriz definitiva está sujeta a validación del Socio Formador.
 */
@Entity(tableName = "criterios")
data class Criterio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val peso: Int,
    val orden: Int
)
