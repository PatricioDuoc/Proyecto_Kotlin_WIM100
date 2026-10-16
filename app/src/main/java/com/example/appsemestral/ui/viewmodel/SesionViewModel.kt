package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.Acciones
import com.example.appsemestral.model.Rol
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.data.repository.HistorialRepository
import com.example.appsemestral.validation.CandidataValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val nombre: CampoState = CampoState(),
    val rol: Rol = Rol.EVALUADORA,
    val intentosFallidos: Int = 0
)

/** Maneja el ingreso y la sesión activa (quién realiza cada acción). */
class SesionViewModel(private val historial: HistorialRepository) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun onNombre(valor: String) = _login.update {
        it.copy(nombre = it.nombre.con(valor, CandidataValidator.validarNombre(valor)))
    }

    fun onRol(rol: Rol) = _login.update { it.copy(rol = rol) }

    /** Valida antes de pedir biometría. @return true si el formulario es válido. */
    fun validar(): Boolean {
        val valor = _login.value.nombre.valor
        val resultado = CandidataValidator.validarNombre(valor)
        _login.update {
            it.copy(
                nombre = it.nombre.con(valor, resultado),
                intentosFallidos = if (resultado.esValido) it.intentosFallidos else it.intentosFallidos + 1
            )
        }
        return resultado.esValido
    }

    fun iniciarSesion(metodo: String) {
        val estado = _login.value
        val nueva = Sesion(estado.nombre.valor.trim().replace(Regex("\\s+"), " "), estado.rol)
        _sesion.value = nueva
        viewModelScope.launch {
            historial.registrar(null, Acciones.INICIO_SESION, "Ingreso como ${nueva.rol.etiqueta} ($metodo)", nueva.nombre)
        }
    }

    fun cerrarSesion() {
        val actual = _sesion.value ?: return
        _sesion.value = null
        _login.value = LoginUiState(rol = actual.rol)
        viewModelScope.launch {
            historial.registrar(null, Acciones.CIERRE_SESION, "Salida del perfil ${actual.rol.etiqueta}", actual.nombre)
        }
    }
}
