package com.listadecompras.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.listadecompras.app.ui.AppNavigation
import com.listadecompras.app.ui.theme.ListaDeComprasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as ListaDeComprasApp).repository
        setContent {
            ListaDeComprasTheme {
                AppNavigation(repository)
            }
        }
    }
}
