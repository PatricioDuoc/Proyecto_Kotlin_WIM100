package com.example.appsemestral.data.repository

import com.example.appsemestral.data.local.dao.HistorialDao
import com.example.appsemestral.model.RegistroHistorial
import kotlinx.coroutines.flow.Flow

/** Punto único para registrar y consultar la trazabilidad de acciones. */
class HistorialRepository(private val dao: HistorialDao) {

    fun observarTodo(): Flow<List<RegistroHistorial>> = dao.observarTodo()

    fun observarPorCandidata(candidataId: Long): Flow<List<RegistroHistorial>> =
        dao.observarPorCandidata(candidataId)

    suspend fun registrar(candidataId: Long?, accion: String, detalle: String, usuario: String) {
        dao.insertar(
            RegistroHistorial(
                candidataId = candidataId,
                accion = accion,
                detalle = detalle,
                usuario = usuario
            )
        )
    }
}
