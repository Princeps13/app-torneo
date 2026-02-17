package com.lucas.app_torneo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lucas.app_torneo.ui.navigation.TorneoNavApp
import com.lucas.app_torneo.ui.theme.AppTorneoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTorneoTheme {
                TorneoNavApp()
            }
        }
    }
}
