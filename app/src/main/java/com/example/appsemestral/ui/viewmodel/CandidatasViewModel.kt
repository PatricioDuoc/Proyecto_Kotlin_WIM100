package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.model.EstadoCandidatura
import com.example.appsemestral.data.repository.CandidataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CandidatasUiState(
    val candidatas: List<Candidata> = emptyList(),
    val filtro: EstadoCandidatura? = null,
    val total: Int = 0,
    val cargando: Boolean = true
)

/** Lista de candidaturas con búsqueda y filtro por estado (reactiva a cambios en Room). */
class CandidatasViewModel(repository: CandidataRepository) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val filtro = MutableStateFlow<EstadoCandidatura?>(null)

    val uiState: StateFlow<CandidatasUiState> =
        combine(repository.observarTodas(), _busqueda, filtro) { lista, texto, estado ->
            val filtradas = lista.filter { c ->
                (estado == null || c.estado == estado) &&
                    (texto.isBlank() || c.nombre.contains(texto.trim(), ignoreCase = true) ||
                        c.area.contains(texto.trim(), ignoreCase = true))
            }
            CandidatasUiState(filtradas, estado, lista.size, cargando = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CandidatasUiState())

    fun onBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun onFiltro(estado: EstadoCandidatura?) {
        filtro.value = estado
    }
}
