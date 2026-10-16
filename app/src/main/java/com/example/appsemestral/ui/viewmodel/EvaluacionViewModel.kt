package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.CalculadoraPuntaje
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.Criterio
import com.example.appsemestral.nativo.Notificador
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.data.repository.EvaluacionDuplicadaException
import com.example.appsemestral.data.repository.EvaluacionRepository
import com.example.appsemestral.navigation.Rutas
import com.example.appsemestral.util.formatearPuntaje
import com.example.appsemestral.validation.EvaluacionValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EvaluacionUiState(
    val candidata: Candidata? = null,
    val criterios: List<Criterio> = emptyList(),
    val puntajes: Map<Long, Int> = emptyMap(),
    val observaciones: Map<Long, String> = emptyMap(),
    val observacionGeneral: String = "",
    val erroresPuntaje: Map<Long, String> = emptyMap(),
    val erroresObservacion: Map<Long, String> = emptyMap(),
    val errorObservacionGeneral: String? = null,
    val intentoEnvio: Boolean = false,
    val intentosFallidos: Int = 0,
    val yaEvaluada: Boolean = false,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val puntajeGuardado: Double? = null,
    val mensaje: String? = null
) {
    /** Puntaje en vivo mientras se completa la matriz. */
    val puntajeTotal: Double get() = CalculadoraPuntaje.calcular(criterios, puntajes)
    val completados: Int get() = criterios.count { puntajes[it.id] != null }
}

/** Aplica la matriz WIM: puntajes por criterio, observaciones y registro sin duplicados. */
class EvaluacionViewModel(
    savedStateHandle: SavedStateHandle,
    private val candidataRepository: CandidataRepository,
    private val evaluacionRepository: EvaluacionRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val candidataId: Long = checkNotNull(savedStateHandle[Rutas.ARG_ID])

    private val _ui = MutableStateFlow(EvaluacionUiState())
    val uiState: StateFlow<EvaluacionUiState> = _ui.asStateFlow()

    fun cargar(evaluadora: String) {
        viewModelScope.launch {
            val candidata = candidataRepository.obtener(candidataId)
            val criterios = evaluacionRepository.obtenerCriterios()
            val yaEvaluada = evaluacionRepository.yaEvaluo(candidataId, evaluadora)
            _ui.update { it.copy(candidata = candidata, criterios = criterios, yaEvaluada = yaEvaluada, cargando = false) }
        }
    }

    fun onPuntaje(criterioId: Long, valor: Int) = _ui.update {
        revalidar(it.copy(puntajes = it.puntajes + (criterioId to valor)))
    }

    fun onObservacion(criterioId: Long, texto: String) = _ui.update {
        revalidar(it.copy(observaciones = it.observaciones + (criterioId to texto)))
    }

    fun onObservacionGeneral(texto: String) = _ui.update {
        revalidar(it.copy(observacionGeneral = texto))
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    /**
     * Antes del primer envío solo se valida la observación del criterio que cambió
     * (para no llenar la pantalla de rojo). Después del primer intento se valida todo en vivo.
     */
    private fun revalidar(s: EvaluacionUiState): EvaluacionUiState =
        if (s.intentoEnvio) aplicarValidacion(s) else s.copy(
            erroresObservacion = s.criterios.mapNotNull { c ->
                EvaluacionValidator.validarObservacionCriterio(s.puntajes[c.id], s.observaciones[c.id].orEmpty())
                    .mensaje?.takeIf { s.observaciones[c.id] != null }?.let { c.id to it }
            }.toMap()
        )

    private fun aplicarValidacion(s: EvaluacionUiState): EvaluacionUiState {
        val erroresPuntaje = s.criterios.mapNotNull { c ->
            EvaluacionValidator.validarPuntaje(s.puntajes[c.id]).mensaje?.let { c.id to it }
        }.toMap()
        val erroresObservacion = s.criterios.mapNotNull { c ->
            EvaluacionValidator.validarObservacionCriterio(s.puntajes[c.id], s.observaciones[c.id].orEmpty())
                .mensaje?.let { c.id to it }
        }.toMap()
        return s.copy(
            erroresPuntaje = erroresPuntaje,
            erroresObservacion = erroresObservacion,
            errorObservacionGeneral = EvaluacionValidator.validarObservacionGeneral(s.observacionGeneral).mensaje
        )
    }

    /** @return true si la matriz está completa y válida (la UI pide confirmación). */
    fun validar(): Boolean {
        val validado = aplicarValidacion(_ui.value.copy(intentoEnvio = true))
        val esValido = validado.erroresPuntaje.isEmpty() &&
            validado.erroresObservacion.isEmpty() &&
            validado.errorObservacionGeneral == null
        _ui.value = if (esValido) validado else validado.copy(intentosFallidos = validado.intentosFallidos + 1)
        return esValido
    }

    fun guardar(evaluadora: String) {
        if (_ui.value.guardando || !validar()) return
        val s = _ui.value
        _ui.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                val total = evaluacionRepository.registrar(
                    candidataId = candidataId,
                    evaluadora = evaluadora,
                    puntajes = s.puntajes,
                    observaciones = s.observaciones,
                    observacionGeneral = s.observacionGeneral
                )
                notificador.evaluacionRegistrada(candidataId, s.candidata?.nombre.orEmpty(), formatearPuntaje(total))
                _ui.update { it.copy(guardando = false, puntajeGuardado = total) }
            } catch (e: EvaluacionDuplicadaException) {
                _ui.update { it.copy(guardando = false, yaEvaluada = true, mensaje = e.message) }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = e.message ?: "No se pudo registrar la evaluación") }
            }
        }
    }
}
