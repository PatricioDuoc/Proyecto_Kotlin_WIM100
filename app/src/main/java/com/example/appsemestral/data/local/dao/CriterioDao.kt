package com.example.appsemestral.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appsemestral.model.Criterio
import kotlinx.coroutines.flow.Flow

@Dao
interface CriterioDao {

    @Query("SELECT * FROM criterios ORDER BY orden")
    fun observarTodos(): Flow<List<Criterio>>

    @Query("SELECT * FROM criterios ORDER BY orden")
    suspend fun obtenerTodos(): List<Criterio>

    @Query("SELECT COUNT(*) FROM criterios")
    suspend fun contar(): Int

    @Insert
    suspend fun insertarTodos(criterios: List<Criterio>)
}
