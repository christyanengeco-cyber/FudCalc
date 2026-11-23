package com.fudcalc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6B45),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7EFC5),
    onPrimaryContainer = Color(0xFF08210F),
    secondary = Color(0xFF4F6352),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E8D4),
    onSecondaryContainer = Color(0xFF0D1F13),
    background = Color(0xFFF7FBF4),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFF7FBF4),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDDE5DA),
    onSurfaceVariant = Color(0xFF414941),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BD3A8),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF14522C),
    onPrimaryContainer = Color(0xFFB7EFC5),
    secondary = Color(0xFFB6CCB9),
    onSecondary = Color(0xFF223527),
    secondaryContainer = Color(0xFF384B3D),
    onSecondaryContainer = Color(0xFFD2E8D4),
    background = Color(0xFF101410),
    onBackground = Color(0xFFE1E4DE),
    surface = Color(0xFF101410),
    onSurface = Color(0xFFE1E4DE),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF),
)

@Composable
fun FudCalcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
