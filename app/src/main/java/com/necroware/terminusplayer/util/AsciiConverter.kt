package com.necroware.terminusplayer.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

data class AsciiPixel(
    val char: Char,
    val color: Color
)

object AsciiConverter {
    // Extended density ramp: sparse/dark symbols -> dense/bright characters
    private const val ASCII_RAMP = " .'`^\",:;Il!i~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"

    fun processBitmap(
        bitmap: Bitmap,
        cols: Int = 64,
        rows: Int = 64
    ): Array<Array<AsciiPixel>> {
        if (bitmap.isRecycled) return Array(rows) { Array(cols) { AsciiPixel(' ', Color.Black) } }
        
        // Downscale bitmap to match target terminal grid
        val scaled = Bitmap.createScaledBitmap(bitmap, cols, rows, true)
        
        val result = Array(rows) { y ->
            Array(cols) { x ->
                val pixel = scaled.getPixel(x, y)
                
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val a = (pixel shr 24) and 0xFF

                // Relative Luminance formula (human eye brightness perception)
                val luminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
                val rampIndex = (luminance * (ASCII_RAMP.length - 1))
                    .toInt()
                    .coerceIn(0, ASCII_RAMP.length - 1)

                AsciiPixel(
                    char = ASCII_RAMP[rampIndex],
                    color = Color(red = r / 255f, green = g / 255f, blue = b / 255f, alpha = a / 255f)
                )
            }
        }
        
        if (scaled != bitmap) scaled.recycle()
        return result
    }
}
