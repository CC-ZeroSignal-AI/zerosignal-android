package com.zerosignal.cognitedge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricPurple,
    tertiary = DarkPurple,
    background = SurfaceDark,
    surface = SurfaceVariantDark,
    surfaceVariant = SurfaceCard,
    onPrimary = SurfaceDark,
    onSecondary = OnSurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceSecondary,
    error = ErrorRed,
    outline = ElectricPurple.copy(alpha = 0.3f)
)

@Composable
fun ZeroSignalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ZeroSignalTypography,
        content = content
    )
}
