package com.joelhellsten.golfswing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1C1C1C),
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    background = Color(0xFFF9F9F9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
)

@Composable
fun GolfSwingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
