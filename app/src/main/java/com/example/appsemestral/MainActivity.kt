package com.example.appsemestral

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.appsemestral.ui.WimApp
import com.example.appsemestral.ui.theme.AppSemestralTheme

/** FragmentActivity (y no ComponentActivity) porque BiometricPrompt la necesita. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppSemestralTheme {
                WimApp()
            }
        }
    }
}
