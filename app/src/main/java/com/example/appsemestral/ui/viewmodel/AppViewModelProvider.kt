package com.example.appsemestral.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.appsemestral.WimApplication

/** Fábrica de ViewModels: les entrega los repositorios del AppContainer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { SesionViewModel(app().container.historialRepository) }
        initializer { CandidatasViewModel(app().container.candidataRepository) }
        initializer { CandidataFormViewModel(app().container.candidataRepository) }
        initializer {
            DetalleCandidataViewModel(
                createSavedStateHandle(),
                app().container.candidataRepository,
                app().container.evaluacionRepository,
                app().container.historialRepository
            )
        }
        initializer {
            EvaluacionViewModel(
                createSavedStateHandle(),
                app().container.candidataRepository,
                app().container.evaluacionRepository,
                app().container.notificador
            )
        }
        initializer {
            ResultadosViewModel(app().container.evaluacionRepository, app().container.historialRepository)
        }
        initializer { HistorialViewModel(app().container.historialRepository) }
    }
}

private fun CreationExtras.app(): WimApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WimApplication
