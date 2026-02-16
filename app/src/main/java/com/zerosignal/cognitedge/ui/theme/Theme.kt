package com.zerosignal.cognitedge.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianColorScheme = darkColorScheme(
    primary         = TextPrimary,       // White primary — buttons, links
    secondary       = Accent,            // Indigo for interactive highlights
    tertiary        = AccentMuted,       // Deeper accent variant
    background      = Obsidian,          // True black
    surface         = Surface1,          // Lifted surface
    surfaceVariant  = Surface2,          // Cards
    onPrimary       = Obsidian,          // Text on white buttons
    onSecondary     = TextPrimary,       // Text on accent
    onBackground    = TextPrimary,       // Text on background
    onSurface       = TextPrimary,       // Text on surfaces
    onSurfaceVariant = TextSecondary,    // Muted text
    error           = Error,
    onError         = TextPrimary,
    outline         = BorderSubtle,
    outlineVariant  = BorderMuted,
    surfaceContainerHighest = Surface3
)

@Composable
fun ZeroSignalTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = ZeroSignalTypography,
        content = content
    )
}
