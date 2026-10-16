package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appsemestral.model.Candidata
import com.example.appsemestral.data.repository.CandidataRepository
import com.example.appsemestral.validation.CandidataValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CandidataFormUiState(
    val nombre: CampoState = CampoState(),
    val correo: CampoState = CampoState(),
    val telefono: CampoState = CampoState(),
    val anios: CampoState = CampoState(),
    val resumen: CampoState = CampoState(),
    val linkedin: CampoState = CampoState(),
    val area: String? = null,
    val errorArea: String? = null,
    val cvUri: String? = null,
    val cvNombre: String? = null,
    val cvTamano: Long? = null,
    val errorCv: String? = null,
    val intentosFallidos: Int = 0,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, correo, telefono, anios, resumen, linkedin).any { it.error != null } ||
            errorArea != null || errorCv != null
}

/**
 * Formulario de registro de candidatura.
 * Toda la lógica de validación vive aquí y en CandidataValidator, NO en los composables.
 */
class CandidataFormViewModel(private val repository: CandidataRepository) : ViewModel() {

    private val _ui = MutableStateFlow(CandidataFormUiState())
    val uiState: StateFlow<CandidataFormUiState> = _ui.asStateFlow()

    // Validación en tiempo real: cada cambio vuelve a validar su campo
    fun onNombre(v: String) = _ui.update { it.copy(nombre = it.nombre.con(v, CandidataValidator.validarNombre(v))) }
    fun onCorreo(v: String) = _ui.update { it.copy(correo = it.correo.con(v, CandidataValidator.validarCorreo(v))) }
    fun onTelefono(v: String) = _ui.update { it.copy(telefono = it.telefono.con(v, CandidataValidator.validarTelefono(v))) }
    fun onAnios(v: String) = _ui.update { it.copy(anios = it.anios.con(v, CandidataValidator.validarAnios(v))) }
    fun onResumen(v: String) = _ui.update { it.copy(resumen = it.resumen.con(v, CandidataValidator.validarResumen(v))) }
    fun onLinkedin(v: String) = _ui.update { it.copy(linkedin = it.linkedin.con(v, CandidataValidator.validarLinkedin(v))) }

    fun onArea(area: String) = _ui.update {
        it.copy(area = area, errorArea = CandidataValidator.validarArea(area).mensaje)
    }

    fun onCv(uri: String, nombre: String?, tamano: Long?) = _ui.update {
        it.copy(cvUri = uri, cvNombre = nombre, cvTamano = tamano, errorCv = CandidataValidator.validarCv(nombre, tamano).mensaje)
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    /** Valida TODOS los campos (incluidos los no tocados) y marca los errores. */
    private fun validarTodo(): Boolean {
        val s = _ui.value
        val validado = s.copy(
            nombre = s.nombre.con(s.nombre.valor, CandidataValidator.validarNombre(s.nombre.valor)),
            correo = s.correo.con(s.correo.valor, CandidataValidator.validarCorreo(s.correo.valor)),
            telefono = s.telefono.con(s.telefono.valor, CandidataValidator.validarTelefono(s.telefono.valor)),
            anios = s.anios.con(s.anios.valor, CandidataValidator.validarAnios(s.anios.valor)),
            resumen = s.resumen.con(s.resumen.valor, CandidataValidator.validarResumen(s.resumen.valor)),
            linkedin = s.linkedin.con(s.linkedin.valor, CandidataValidator.validarLinkedin(s.linkedin.valor)),
            errorArea = CandidataValidator.validarArea(s.area).mensaje,
            errorCv = CandidataValidator.validarCv(s.cvNombre, s.cvTamano).mensaje
        )
        val esValido = !validado.hayErrores
        _ui.value = if (esValido) validado else validado.copy(intentosFallidos = s.intentosFallidos + 1)
        return esValido
    }

    fun guardar(registradaPor: String) {
        if (_ui.value.guardando || !validarTodo()) return
        val s = _ui.value
        _ui.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                // Regla de negocio: no se registra dos veces el mismo correo
                if (repository.existeCorreo(s.correo.valor)) {
                    _ui.update {
                        it.copy(
                            guardando = false,
                            correo = it.correo.copy(error = "Ya existe una candidatura con este correo"),
                            intentosFallidos = it.intentosFallidos + 1
                        )
                    }
                    return@launch
                }
                repository.registrar(
                    Candidata(
                        nombre = s.nombre.valor.trim().replace(Regex("\\s+"), " "),
                        correo = s.correo.valor.trim().lowercase(),
                        telefono = CandidataValidator.formatearTelefono(s.telefono.valor),
                        area = s.area.orEmpty(),
                        aniosExperiencia = s.anios.valor.trim().toInt(),
                        resumenExperiencia = s.resumen.valor.trim(),
                        linkedinUrl = s.linkedin.valor.trim(),
                        cvUri = s.cvUri,
                        cvNombre = s.cvNombre,
                        registradaPor = registradaPor
                    )
                )
                _ui.update { it.copy(guardando = false, guardado = true) }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = "No se pudo guardar: ${e.message}") }
            }
        }
    }
}
