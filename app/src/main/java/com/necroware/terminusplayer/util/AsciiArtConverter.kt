package com.necroware.terminusplayer.util

import android.graphics.Bitmap

/**
 * Converts album art Bitmaps into terminal ASCII blocks using luminance mapping.
 */
object AsciiArtConverter {
    private val ASCII_RAMP = charArrayOf(' ', '.', ':', '-', '=', '+', '*', '#', '%', '@', '█')

    fun convertToAscii(bitmap: Bitmap, width: Int = 40, height: Int = 20): String {
        if (bitmap.isRecycled) return ""
        
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)
        val builder = StringBuilder()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = scaled.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                
                // Photometric Luminance formula
                val luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                val charIndex = (luminance * (ASCII_RAMP.size - 1)).toInt().coerceIn(0, ASCII_RAMP.size - 1)
                builder.append(ASCII_RAMP[charIndex])
            }
            if (y < height - 1) builder.append("\n")
        }
        
        if (scaled != bitmap) scaled.recycle()
        return builder.toString()
    }
}
