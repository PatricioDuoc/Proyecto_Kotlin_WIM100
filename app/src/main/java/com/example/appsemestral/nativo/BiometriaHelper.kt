package com.example.appsemestral.nativo

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * RECURSO NATIVO: biometría (huella / rostro) o credencial del dispositivo (PIN, patrón).
 * Permiso: USE_BIOMETRIC (normal, se otorga al instalar; no requiere diálogo).
 */
object BiometriaHelper {

    private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    enum class Disponibilidad(val etiqueta: String) {
        DISPONIBLE("Disponible"),
        SIN_REGISTRO("Sin huella ni bloqueo configurado"),
        NO_DISPONIBLE("No disponible en este dispositivo")
    }

    fun disponibilidad(context: Context): Disponibilidad =
        when (BiometricManager.from(context).canAuthenticate(AUTENTICADORES)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Disponibilidad.DISPONIBLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Disponibilidad.SIN_REGISTRO
            else -> Disponibilidad.NO_DISPONIBLE
        }

    fun autenticar(
        activity: FragmentActivity,
        titulo: String,
        subtitulo: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onExito()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
                // onAuthenticationFailed: el propio diálogo permite reintentar
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setAllowedAuthenticators(AUTENTICADORES) // con DEVICE_CREDENTIAL no se usa botón negativo
            .build()
        prompt.authenticate(info)
    }
}
