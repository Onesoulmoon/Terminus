package com.necroware.terminusplayer.ui.theme

import androidx.compose.ui.graphics.Color
import com.necroware.terminusplayer.data.prefs.ThemePresetId

/**
 * A full palette, not just an accent swap — background/surface/border/text
 * all move together so each preset reads as a deliberate scheme rather than
 * one color dropped onto the default dark palette.
 */
data class ThemePreset(
    val id: ThemePresetId,
    val label: String,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color
)

val ThemePresets: List<ThemePreset> = listOf(
    ThemePreset(
        id = ThemePresetId.TERMINAL,
        label = "TERMINAL",
        background = Color(0xFF1A1A1A), // Dark Grey
        surface = Color(0xFF242424),
        surfaceElevated = Color(0xFF2F2F2F),
        border = Color(0xFF3A3A3A),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCCCCCC),
        textMuted = Color(0xFF888888),
        accent = Color(0xFFFF9800), // Orange
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.VECTOR,
        label = "VECTOR",
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surfaceElevated = Color(0xFF141414),
        border = Color(0xFF333333),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFAAAAAA),
        textMuted = Color(0xFF555555),
        accent = Color(0xFFFFFFFF),
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.REBECCA,
        label = "REBECCA",
        background = Color(0xFF000000),
        surface = Color(0xFF081010),
        surfaceElevated = Color(0xFF101818),
        border = Color(0xFF1A2F2F),
        textPrimary = Color(0xFFE0FFFF),
        textSecondary = Color(0xFF22D3EE),
        textMuted = Color(0xFF164E63),
        accent = Color(0xFFFDE047), // Yellow
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.DUNE,
        label = "DUNE",
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF5F5F4),
        surfaceElevated = Color(0xFFE7E5E4),
        border = Color(0xFFD6D3D1),
        textPrimary = Color(0xFF1C1917),
        textSecondary = Color(0xFF44403C),
        textMuted = Color(0xFF78716C),
        accent = Color(0xFFEAB308), // Darker Yellow
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.HEX,
        label = "HEX",
        background = Color(0xFF000000),
        surface = Color(0xFF051008),
        surfaceElevated = Color(0xFF0A1A0F),
        border = Color(0xFF14301A),
        textPrimary = Color(0xFFDCFCE7),
        textSecondary = Color(0xFF4ADE80),
        textMuted = Color(0xFF166534),
        accent = Color(0xFF22C55E), // Green
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.LUCY,
        label = "LUCY",
        background = Color(0xFF0F0716),
        surface = Color(0xFF1A0D25),
        surfaceElevated = Color(0xFF261435),
        border = Color(0xFF3B1F50),
        textPrimary = Color(0xFFF5F3FF),
        textSecondary = Color(0xFFA78BFA),
        textMuted = Color(0xFF5B21B6),
        accent = Color(0xFF22D3EE), // Cyan
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.MAINE,
        label = "MAINE",
        background = Color(0xFF100505),
        surface = Color(0xFF1A0A0A),
        surfaceElevated = Color(0xFF261010),
        border = Color(0xFF451010),
        textPrimary = Color(0xFFFEE2E2),
        textSecondary = Color(0xFFEF4444),
        textMuted = Color(0xFF7F1D1D),
        accent = Color(0xFF22D3EE), // Cyan
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.FLATLINE,
        label = "FLATLINE",
        background = Color(0xFF000000),
        surface = Color(0xFF1A0505),
        surfaceElevated = Color(0xFF2D0A0A),
        border = Color(0xFF450A0A),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFF87171),
        textMuted = Color(0xFF7F1D1D),
        accent = Color(0xFFDC2626), // Red
        onAccent = Color(0xFF000000)
    ),
    ThemePreset(
        id = ThemePresetId.WIZ,
        label = "WIZ",
        background = Color(0xFF000000),
        surface = Color(0xFF0F0F05),
        surfaceElevated = Color(0xFF1A1A0A),
        border = Color(0xFF33330A),
        textPrimary = Color(0xFFFDFCEA),
        textSecondary = Color(0xFFEAB308),
        textMuted = Color(0xFF713F12),
        accent = Color(0xFFFACC15), // Yellow
        onAccent = Color(0xFF000000)
    )
)

fun themePresetById(id: ThemePresetId): ThemePreset =
    ThemePresets.firstOrNull { it.id == id } ?: ThemePresets.first()
