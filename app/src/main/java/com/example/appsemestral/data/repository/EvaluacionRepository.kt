package com.example.appsemestral.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.appsemestral.data.local.WimDatabase
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.CalculadoraPuntaje
import com.example.appsemestral.model.Criterio
import com.example.appsemestral.model.DetallePuntaje
import com.example.appsemestral.model.EstadoCandidatura
import com.example.appsemestral.model.Evaluacion
import com.example.appsemestral.model.PuntajeCriterio
import com.example.appsemestral.model.ResultadoConsolidado
import kotlinx.coroutines.flow.Flow

/** Se lanza cuando una evaluadora intenta evaluar dos veces a la misma candidata. */
class EvaluacionDuplicadaException(mensaje: String) : Exception(mensaje)

class EvaluacionRepository(
    private val db: WimDatabase,
    private val historial: HistorialRepository
) {
    private val evaluacionDao = db.evaluacionDao()
    private val criterioDao = db.criterioDao()
    private val candidataDao = db.candidataDao()

    fun observarCriterios(): Flow<List<Criterio>> = criterioDao.observarTodos()

    suspend fun obtenerCriterios(): List<Criterio> = criterioDao.obtenerTodos()

    suspend fun asegurarCriterios(criterios: List<Criterio>) {
        if (criterioDao.contar() == 0) criterioDao.insertarTodos(criterios)
    }

    fun observarPorCandidata(candidataId: Long): Flow<List<Evaluacion>> =
        evaluacionDao.observarPorCandidata(candidataId)

    fun observarDetallePuntajes(candidataId: Long): Flow<List<DetallePuntaje>> =
        evaluacionDao.observarDetallePuntajes(candidataId)

    fun observarConsolidado(): Flow<List<ResultadoConsolidado>> = evaluacionDao.observarConsolidado()

    suspend fun yaEvaluo(candidataId: Long, evaluadora: String): Boolean =
        evaluacionDao.contarPorEvaluadora(candidataId, Evaluacion.normalizarClave(evaluadora)) > 0

    /**
     * Registra una evaluación completa dentro de una transacción:
     * 1) verifica duplicado, 2) calcula puntaje, 3) guarda evaluación y puntajes,
     * 4) actualiza el estado de la candidata y 5) deja registro en el historial.
     * Si algo falla, no se guarda nada (todo o nada).
     * @return puntaje total (0..100)
     */
    suspend fun registrar(
        candidataId: Long,
        evaluadora: String,
        puntajes: Map<Long, Int>,
        observaciones: Map<Long, String>,
        observacionGeneral: String
    ): Double {
        val clave = Evaluacion.normalizarClave(evaluadora)
        try {
            return db.withTransaction {
                val candidata = candidataDao.obtenerPorId(candidataId)
                    ?: throw IllegalStateException("La candidatura no existe")
                if (candidata.estado.estaResuelta) {
                    throw IllegalStateException("La candidatura ya fue resuelta por Dirección")
                }
                if (evaluacionDao.contarPorEvaluadora(candidataId, clave) > 0) {
                    throw EvaluacionDuplicadaException("Ya registraste una evaluación para ${candidata.nombre}")
                }
                val criterios = criterioDao.obtenerTodos()
                val total = CalculadoraPuntaje.calcular(criterios, puntajes)

                val evaluacionId = evaluacionDao.insertar(
                    Evaluacion(
                        candidataId = candidataId,
                        evaluadora = evaluadora.trim(),
                        evaluadoraClave = clave,
                        puntajeTotal = total,
                        observacionGeneral = observacionGeneral.trim()
                    )
                )
                evaluacionDao.insertarPuntajes(
                    criterios.map { criterio ->
                        PuntajeCriterio(
                            evaluacionId = evaluacionId,
                            criterioId = criterio.id,
                            puntaje = puntajes.getValue(criterio.id),
                            observacion = observaciones[criterio.id].orEmpty().trim()
                        )
                    }
                )

                val cantidad = evaluacionDao.contarPorCandidata(candidataId)
                val nuevoEstado = if (cantidad >= Evaluacion.MINIMO_PARA_CONSOLIDAR) {
                    EstadoCandidatura.EVALUADA
                } else {
                    EstadoCandidatura.EN_EVALUACION
                }
                candidataDao.actualizarEstado(candidataId, nuevoEstado)

                historial.registrar(
                    candidataId = candidataId,
                    accion = Acciones.EVALUACION,
                    detalle = "${candidata.nombre}: ${"%.1f".format(total)} pts · estado ${nuevoEstado.etiqueta}",
                    usuario = evaluadora.trim()
                )
                total
            }
        } catch (e: EvaluacionDuplicadaException) {
            registrarIntentoDuplicado(candidataId, evaluadora)
            throw e
        } catch (e: SQLiteConstraintException) {
            // Segunda barrera: el índice único de la BD también impide el duplicado
            registrarIntentoDuplicado(candidataId, evaluadora)
            throw EvaluacionDuplicadaException("Ya existe una evaluación tuya para esta candidata")
        }
    }

    private suspend fun registrarIntentoDuplicado(candidataId: Long, evaluadora: String) {
        historial.registrar(
            candidataId = candidataId,
            accion = Acciones.INTENTO_DUPLICADO,
            detalle = "El sistema bloqueó una segunda evaluación",
            usuario = evaluadora.trim()
        )
    }
}
