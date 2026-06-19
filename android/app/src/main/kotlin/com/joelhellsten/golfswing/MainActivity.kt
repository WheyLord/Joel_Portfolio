package com.joelhellsten.golfswing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.joelhellsten.golfswing.navigation.AppNavigation
import com.joelhellsten.golfswing.ui.theme.GolfSwingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GolfSwingTheme {
                AppNavigation()
            }
        }
    }
}
