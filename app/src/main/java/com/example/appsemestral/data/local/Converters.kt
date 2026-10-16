package com.example.appsemestral.data.local

import androidx.room.TypeConverter
import com.example.appsemestral.model.EstadoCandidatura

/** Room guarda el enum como texto (su nombre) y lo reconstruye al leer. */
class Converters {
    @TypeConverter
    fun desdeEstado(estado: EstadoCandidatura): String = estado.name

    @TypeConverter
    fun haciaEstado(valor: String): EstadoCandidatura = EstadoCandidatura.valueOf(valor)
}
