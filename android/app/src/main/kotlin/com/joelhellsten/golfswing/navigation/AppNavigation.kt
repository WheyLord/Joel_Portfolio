package com.joelhellsten.golfswing.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joelhellsten.golfswing.ui.capture.CaptureScreen
import com.joelhellsten.golfswing.ui.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Capture : Screen("capture")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onRecordSwing = { navController.navigate(Screen.Capture.route) })
        }
        composable(Screen.Capture.route) {
            CaptureScreen(onBack = { navController.popBackStack() })
        }
    }
}
