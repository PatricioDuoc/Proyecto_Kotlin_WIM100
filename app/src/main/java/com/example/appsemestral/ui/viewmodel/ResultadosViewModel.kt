package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.ResultadoConsolidado
import com.example.appsemestral.data.repository.EvaluacionRepository
import com.example.appsemestral.data.repository.HistorialRepository
import com.example.appsemestral.util.formatearPuntaje
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResultadosUiState(
    val resultados: List<ResultadoConsolidado> = emptyList(),
    val totalCandidatas: Int = 0,
    val conEvaluacion: Int = 0,
    val promedioGeneral: Double? = null,
    val cargando: Boolean = true
)

/** Resultados consolidados para Dirección (y consulta de evaluadoras). */
class ResultadosViewModel(
    evaluacionRepository: EvaluacionRepository,
    private val historialRepository: HistorialRepository
) : ViewModel() {

    val uiState: StateFlow<ResultadosUiState> = evaluacionRepository.observarConsolidado()
        .map { lista ->
            val evaluadas = lista.filter { it.totalEvaluaciones > 0 }
            ResultadosUiState(
                resultados = lista,
                totalCandidatas = lista.size,
                conEvaluacion = evaluadas.size,
                promedioGeneral = evaluadas.mapNotNull { it.promedio }.takeIf { it.isNotEmpty() }?.average(),
                cargando = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultadosUiState())

    fun textoCompartir(): String = buildString {
        val s = uiState.value
        appendLine("WIM 100 · Resultados consolidados (datos ficticios)")
        appendLine("Candidaturas: ${s.totalCandidatas} · Con evaluación: ${s.conEvaluacion}")
        appendLine()
        s.resultados.forEachIndexed { i, r ->
            appendLine("${i + 1}. ${r.nombre} — ${formatearPuntaje(r.promedio)} pts (${r.totalEvaluaciones} eval.) · ${r.estado.etiqueta}")
        }
    }

    fun registrarCompartir(usuario: String) {
        viewModelScope.launch {
            historialRepository.registrar(null, Acciones.COMPARTIR, "Se compartió el ranking consolidado", usuario)
        }
    }
}
