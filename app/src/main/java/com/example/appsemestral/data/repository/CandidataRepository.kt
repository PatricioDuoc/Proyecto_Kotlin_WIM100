package com.example.appsemestral.data.repository

import com.example.appsemestral.data.local.dao.CandidataDao
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.EstadoCandidatura
import kotlinx.coroutines.flow.Flow

/** Acceso a candidaturas. Los ViewModels nunca hablan directo con el DAO. */
class CandidataRepository(
    private val dao: CandidataDao,
    private val historial: HistorialRepository
) {
    fun observarTodas(): Flow<List<Candidata>> = dao.observarTodas()

    fun observarPorId(id: Long): Flow<Candidata?> = dao.observarPorId(id)

    suspend fun obtener(id: Long): Candidata? = dao.obtenerPorId(id)

    suspend fun contar(): Int = dao.contar()

    suspend fun existeCorreo(correo: String): Boolean = dao.contarPorCorreo(correo.trim()) > 0

    suspend fun registrar(candidata: Candidata): Long {
        val id = dao.insertar(candidata)
        historial.registrar(
            candidataId = id,
            accion = Acciones.REGISTRO,
            detalle = "Se registró a ${candidata.nombre} · ${candidata.area}",
            usuario = candidata.registradaPor
        )
        return id
    }

    /** Dirección cierra la candidatura como seleccionada o no seleccionada. */
    suspend fun resolver(id: Long, estado: EstadoCandidatura, usuario: String) {
        require(estado.estaResuelta) { "Solo se puede resolver como Seleccionada o No seleccionada" }
        val actual = dao.obtenerPorId(id) ?: return
        dao.actualizarEstado(id, estado)
        historial.registrar(
            candidataId = id,
            accion = Acciones.RESOLUCION,
            detalle = "${actual.nombre}: ${actual.estado.etiqueta} → ${estado.etiqueta}",
            usuario = usuario
        )
    }
}
