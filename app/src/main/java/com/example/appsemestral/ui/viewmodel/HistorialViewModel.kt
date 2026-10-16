package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.RegistroHistorial
import com.example.appsemestral.data.repository.HistorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistorialUiState(
    val registros: List<RegistroHistorial> = emptyList(),
    val cargando: Boolean = true
)

class HistorialViewModel(repository: HistorialRepository) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val uiState: StateFlow<HistorialUiState> =
        combine(repository.observarTodo(), _busqueda) { lista, texto ->
            val t = texto.trim()
            HistorialUiState(
                registros = if (t.isEmpty()) lista else lista.filter {
                    it.accion.contains(t, true) || it.detalle.contains(t, true) || it.usuario.contains(t, true)
                },
                cargando = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    fun onBusqueda(texto: String) {
        _busqueda.value = texto
    }
}
