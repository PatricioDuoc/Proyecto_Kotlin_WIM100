package com.example.appsemestral.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.appsemestral.model.Sesion
import com.example.appsemestral.ui.screens.CandidataFormScreen
import com.example.appsemestral.ui.screens.CandidatasScreen
import com.example.appsemestral.ui.screens.DetalleCandidataScreen
import com.example.appsemestral.ui.screens.EvaluacionScreen
import com.example.appsemestral.ui.screens.HistorialScreen
import com.example.appsemestral.ui.screens.LoginScreen
import com.example.appsemestral.ui.screens.PerfilScreen
import com.example.appsemestral.ui.screens.ResultadosScreen
import com.example.appsemestral.ui.viewmodel.SesionViewModel

private const val DURACION = 320

/** Grafo de navegación con transiciones animadas entre pantallas. */
@Composable
fun WimNavHost(
    navController: NavHostController,
    sesionViewModel: SesionViewModel,
    sesion: Sesion?,
    mostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val argumentoId = listOf(navArgument(Rutas.ARG_ID) { type = NavType.LongType })

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(DURACION)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION))
        },
        exitTransition = { fadeOut(tween(DURACION / 2)) },
        popEnterTransition = { fadeIn(tween(DURACION)) },
        popExitTransition = {
            fadeOut(tween(DURACION / 2)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION))
        }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = sesionViewModel,
                mostrarMensaje = mostrarMensaje,
                onIngreso = {
                    navController.navigate(Rutas.CANDIDATAS) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pestañas principales: transición de solo desvanecimiento
        composable(Rutas.CANDIDATAS, enterTransition = { fadeIn(tween(DURACION)) }) {
            if (sesion != null) {
                CandidatasScreen(
                    sesion = sesion,
                    onNueva = { navController.navigate(Rutas.NUEVA_CANDIDATA) },
                    onAbrir = { id -> navController.navigate(Rutas.detalle(id)) }
                )
            }
        }
        composable(Rutas.RESULTADOS, enterTransition = { fadeIn(tween(DURACION)) }) {
            if (sesion != null) {
                ResultadosScreen(
                    sesion = sesion,
                    onAbrir = { id -> navController.navigate(Rutas.detalle(id)) }
                )
            }
        }
        composable(Rutas.HISTORIAL, enterTransition = { fadeIn(tween(DURACION)) }) {
            HistorialScreen()
        }
        composable(Rutas.PERFIL, enterTransition = { fadeIn(tween(DURACION)) }) {
            if (sesion != null) {
                PerfilScreen(
                    sesion = sesion,
                    mostrarMensaje = mostrarMensaje,
                    onCerrarSesion = {
                        sesionViewModel.cerrarSesion()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Rutas.NUEVA_CANDIDATA) {
            if (sesion != null) {
                CandidataFormScreen(
                    sesion = sesion,
                    onVolver = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }
        }
        composable(Rutas.DETALLE, arguments = argumentoId) {
            if (sesion != null) {
                DetalleCandidataScreen(
                    sesion = sesion,
                    onVolver = { navController.popBackStack() },
                    onEvaluar = { id -> navController.navigate(Rutas.evaluar(id)) },
                    mostrarMensaje = mostrarMensaje
                )
            }
        }
        composable(Rutas.EVALUAR, arguments = argumentoId) {
            if (sesion != null) {
                EvaluacionScreen(
                    sesion = sesion,
                    onVolver = { navController.popBackStack() },
                    mostrarMensaje = mostrarMensaje
                )
            }
        }
    }
}
