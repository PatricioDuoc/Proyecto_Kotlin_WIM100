package com.example.appsemestral

import android.app.Application
import com.example.appsemestral.data.local.DatosSemilla
import com.example.appsemestral.di.AppContainer
import com.example.appsemestral.nativo.NotificacionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WimApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificacionHelper.crearCanal(this)
        scope.launch { DatosSemilla.poblarSiVacia(this@WimApplication, container) }
    }
}
