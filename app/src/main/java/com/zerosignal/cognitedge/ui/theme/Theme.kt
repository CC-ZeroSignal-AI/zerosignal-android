package com.zerosignal.cognitedge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = Purple40,
    tertiary = PurpleDark,
    background = SurfaceDark,
    surface = SurfaceVariantDark,
    onPrimary = SurfaceDark,
    onSecondary = OnSurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

@Composable
fun ZeroSignalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ZeroSignalTypography,
        content = content
    )
}
