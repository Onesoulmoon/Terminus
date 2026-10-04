package com.necroware.terminusplayer.ui.theme

import androidx.compose.ui.graphics.Color
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.util.TerminalPalette

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
) {
    val palette: TerminalPalette
        get() = when (id) {
            ThemePresetId.TERMINAL -> TerminalPalette(
                primaryAccent = Color(0xFFFF9800),   // Orange
                secondaryAccent = Color(0xFF00FFC2), // Neon Cyan
                tertiaryAccent = Color(0xFFFFCC00),  // Gold Yellow
                highlightAccent = Color(0xFFFFFFFF), // White
                mutedAccent = Color(0xFF3A3A3A)      // Dark Grey Border
            )
            ThemePresetId.VECTOR -> TerminalPalette(
                primaryAccent = Color(0xFFFFFFFF),   // Vector White
                secondaryAccent = Color(0xFFAAAAAA), // Light Grey
                tertiaryAccent = Color(0xFFCCCCCC),  // Bright Silver
                highlightAccent = Color(0xFFFFFFFF), // White
                mutedAccent = Color(0xFF333333)      // Dark Grey
            )
            ThemePresetId.REBECCA -> TerminalPalette(
                primaryAccent = Color(0xFFFDE047),   // Yellow Accent
                secondaryAccent = Color(0xFF22D3EE), // Cyan Secondary
                tertiaryAccent = Color(0xFFE0FFFF),  // Light Cyan
                highlightAccent = Color(0xFF38BDF8), // Sky Blue
                mutedAccent = Color(0xFF1A2F2F)      // Dark Teal
            )
            ThemePresetId.DUNE -> TerminalPalette(
                primaryAccent = Color(0xFFEAB308),   // Sand Gold
                secondaryAccent = Color(0xFFCA8A04), // Warm Amber
                tertiaryAccent = Color(0xFF44403C),  // Charcoal
                highlightAccent = Color(0xFF1C1917), // Dark Brown Text
                mutedAccent = Color(0xFFD6D3D1)      // Warm Stone
            )
            ThemePresetId.HEX -> TerminalPalette(
                primaryAccent = Color(0xFF22C55E),   // Matrix Green
                secondaryAccent = Color(0xFF4ADE80), // Light Green
                tertiaryAccent = Color(0xFFDCFCE7),  // Soft Mint
                highlightAccent = Color(0xFF16A34A), // Deep Green
                mutedAccent = Color(0xFF14301A)      // Dark Green Border
            )
            ThemePresetId.LUCY -> TerminalPalette(
                primaryAccent = Color(0xFF22D3EE),   // Cyber Cyan
                secondaryAccent = Color(0xFFA78BFA), // Electric Violet
                tertiaryAccent = Color(0xFFF5F3FF),  // Soft Lavender
                highlightAccent = Color(0xFFE879F9), // Neon Pink
                mutedAccent = Color(0xFF3B1F50)      // Deep Purple Border
            )
            ThemePresetId.MAINE -> TerminalPalette(
                primaryAccent = Color(0xFF22D3EE),   // Ice Cyan
                secondaryAccent = Color(0xFFEF4444), // Crimson Red
                tertiaryAccent = Color(0xFFFEE2E2),  // Soft Coral
                highlightAccent = Color(0xFFF43F5E), // Neon Coral
                mutedAccent = Color(0xFF451010)      // Dark Crimson Border
            )
            ThemePresetId.FLATLINE -> TerminalPalette(
                primaryAccent = Color(0xFFDC2626),   // Flatline Red
                secondaryAccent = Color(0xFFF87171), // Soft Light Red
                tertiaryAccent = Color(0xFFEF4444),  // Bright Crimson
                highlightAccent = Color(0xFFFFFFFF), // High Contrast White
                mutedAccent = Color(0xFF450A0A)      // Dark Red Border
            )
            ThemePresetId.DYNAMIC -> TerminalPalette(
                primaryAccent = accent,
                secondaryAccent = textSecondary,
                tertiaryAccent = border,
                highlightAccent = textPrimary,
                mutedAccent = textMuted
            )
        }
}

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
    )
)

fun themePresetById(id: ThemePresetId): ThemePreset =
    ThemePresets.firstOrNull { it.id == id } ?: ThemePresets.first()
