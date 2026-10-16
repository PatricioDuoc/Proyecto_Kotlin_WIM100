package com.example.appsemestral.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Trazabilidad: cada acción relevante queda registrada con quién, qué y cuándo.
 * No tiene FK a candidatas para que el historial se conserve siempre.
 */
@Entity(tableName = "historial", indices = [Index("candidataId")])
data class RegistroHistorial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val candidataId: Long?,
    val accion: String,
    val detalle: String,
    val usuario: String,
    val fecha: Long = System.currentTimeMillis()
)

/** Nombres de acciones usados en el historial. */
object Acciones {
    const val INICIO_SESION = "Inicio de sesión"
    const val CIERRE_SESION = "Cierre de sesión"
    const val REGISTRO = "Registro de candidatura"
    const val EVALUACION = "Evaluación registrada"
    const val INTENTO_DUPLICADO = "Intento de evaluación duplicada"
    const val RESOLUCION = "Resolución de candidatura"
    const val CONSULTA_CV = "Consulta de CV"
    const val CONSULTA_PERFIL = "Consulta de perfil profesional"
    const val COMPARTIR = "Información compartida"
}
