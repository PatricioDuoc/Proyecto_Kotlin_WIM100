package com.example.appsemestral.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appsemestral.model.DetallePuntaje
import com.example.appsemestral.model.Evaluacion
import com.example.appsemestral.model.PuntajeCriterio
import com.example.appsemestral.model.ResultadoConsolidado
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluacionDao {

    /** ABORT: si la evaluadora ya evaluó a la candidata, el índice único lanza una excepción. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(evaluacion: Evaluacion): Long

    @Insert
    suspend fun insertarPuntajes(puntajes: List<PuntajeCriterio>)

    @Query("SELECT COUNT(*) FROM evaluaciones WHERE candidataId = :candidataId AND evaluadoraClave = :clave")
    suspend fun contarPorEvaluadora(candidataId: Long, clave: String): Int

    @Query("SELECT COUNT(*) FROM evaluaciones WHERE candidataId = :candidataId")
    suspend fun contarPorCandidata(candidataId: Long): Int

    @Query("SELECT * FROM evaluaciones WHERE candidataId = :candidataId ORDER BY fecha DESC")
    fun observarPorCandidata(candidataId: Long): Flow<List<Evaluacion>>

    @Query(
        """
        SELECT p.evaluacionId AS evaluacionId, c.nombre AS criterioNombre, c.peso AS peso,
               p.puntaje AS puntaje, p.observacion AS observacion
        FROM puntajes_criterio p
        INNER JOIN criterios c ON c.id = p.criterioId
        INNER JOIN evaluaciones e ON e.id = p.evaluacionId
        WHERE e.candidataId = :candidataId
        ORDER BY c.orden
        """
    )
    fun observarDetallePuntajes(candidataId: Long): Flow<List<DetallePuntaje>>

    /** Resultados consolidados: promedio de puntaje y cantidad de evaluaciones por candidata. */
    @Query(
        """
        SELECT c.id AS candidataId, c.nombre AS nombre, c.area AS area, c.estado AS estado,
               COUNT(e.id) AS totalEvaluaciones, AVG(e.puntajeTotal) AS promedio
        FROM candidatas c
        LEFT JOIN evaluaciones e ON e.candidataId = c.id
        GROUP BY c.id
        ORDER BY promedio DESC, c.nombre ASC
        """
    )
    fun observarConsolidado(): Flow<List<ResultadoConsolidado>>
}
