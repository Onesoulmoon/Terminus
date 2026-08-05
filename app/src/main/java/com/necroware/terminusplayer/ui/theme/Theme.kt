package com.necroware.terminusplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Deliberately NOT using dynamicColorScheme / Material You here — TERMINUS
 * always renders one of its own terminal palettes regardless of system
 * wallpaper. Every screen reads colors off MaterialTheme.colorScheme rather
 * than the theme/Color.kt constants directly, so swapping the whole
 * [ThemePreset] here is enough to re-skin the entire app.
 */
private fun terminalColorScheme(preset: ThemePreset) = darkColorScheme(
    primary = preset.accent,
    onPrimary = preset.onAccent,
    secondary = preset.accent,
    onSecondary = preset.onAccent,
    background = preset.background,
    onBackground = preset.textPrimary,
    surface = preset.surface,
    onSurface = preset.textPrimary,
    surfaceVariant = preset.surfaceElevated,
    onSurfaceVariant = preset.textSecondary,
    outline = preset.border,
    error = RedAccent,
    onError = preset.onAccent
)

@Composable
fun TerminusTheme(
    preset: ThemePreset = ThemePresets.first(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = terminalColorScheme(preset),
        typography = TerminusTypography,
        content = content
    )
}
