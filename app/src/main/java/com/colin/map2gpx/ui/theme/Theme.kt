package com.colin.map2gpx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentGreen,
    background = BackgroundLight,
    onPrimary = Color.White,
    onBackground = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    secondary = AccentGreen,
    background = BackgroundDark,
    onPrimary = Color.White,
    onBackground = TextPrimary
)

@Composable
fun Map2GpxTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}