package com.necroware.terminusplayer.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette

data class TerminalPalette(
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val highlightAccent: Color,
    val mutedAccent: Color
) {
    companion object {
        val DefaultFallback = TerminalPalette(
            primaryAccent = Color(0xFF00FFC2),   // Neon Cyan
            secondaryAccent = Color(0xFFFF0055), // Cyber Pink
            tertiaryAccent = Color(0xFFFFCC00),  // Gold Yellow
            highlightAccent = Color(0xFF9966FF), // Electric Purple
            mutedAccent = Color(0xFF336644)      // Dim Matrix Green
        )
    }
}

object PaletteExtractor {

    fun extractAccentColor(bitmap: Bitmap?, defaultColor: Color = Color(0xFF00FFC2)): Color {
        if (bitmap == null) return defaultColor
        val palette = Palette.from(bitmap).generate()

        val rgb = palette.vibrantSwatch?.rgb
            ?: palette.lightVibrantSwatch?.rgb
            ?: palette.dominantSwatch?.rgb

        return rgb?.let { Color(it) } ?: defaultColor
    }

    fun generateTerminalPalette(bitmap: Bitmap?): TerminalPalette {
        if (bitmap == null) return TerminalPalette.DefaultFallback

        val palette = Palette.from(bitmap).generate()

        val vibrant = palette.getVibrantColor(TerminalPalette.DefaultFallback.primaryAccent.toArgb())
        val lightVibrant = palette.getLightVibrantColor(TerminalPalette.DefaultFallback.secondaryAccent.toArgb())
        val darkVibrant = palette.getDarkVibrantColor(TerminalPalette.DefaultFallback.tertiaryAccent.toArgb())
        val muted = palette.getMutedColor(TerminalPalette.DefaultFallback.highlightAccent.toArgb())
        val lightMuted = palette.getLightMutedColor(TerminalPalette.DefaultFallback.mutedAccent.toArgb())

        val pColor = Color(vibrant)
        var sColor = Color(lightVibrant)
        if (sColor == pColor) sColor = TerminalPalette.DefaultFallback.secondaryAccent

        var tColor = Color(darkVibrant)
        if (tColor == pColor || tColor == sColor) tColor = TerminalPalette.DefaultFallback.tertiaryAccent

        var hColor = Color(muted)
        if (hColor == pColor || hColor == sColor) hColor = TerminalPalette.DefaultFallback.highlightAccent

        return TerminalPalette(
            primaryAccent = pColor,
            secondaryAccent = sColor,
            tertiaryAccent = tColor,
            highlightAccent = hColor,
            mutedAccent = Color(lightMuted)
        )
    }
}
