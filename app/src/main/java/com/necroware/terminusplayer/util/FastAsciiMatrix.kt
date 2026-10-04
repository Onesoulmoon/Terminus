package com.necroware.terminusplayer.util

import android.graphics.Bitmap
import kotlin.math.pow

class FastAsciiMatrix(val gridSize: Int) {
    // Flat primitive arrays prevent Object allocations during render loops
    val chars = CharArray(gridSize * gridSize)
    val colors = IntArray(gridSize * gridSize)

    private val asciiRamp = " .'`^\",:;Il!i~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"

    fun updateFromBitmap(bitmap: Bitmap) {
        val scaled = Bitmap.createScaledBitmap(bitmap, gridSize, gridSize, true)
        val pixels = IntArray(gridSize * gridSize)
        scaled.getPixels(pixels, 0, gridSize, 0, 0, gridSize, gridSize)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Ultra-luminance gamma correction for maximum daylight visibility
            val rawLuminance = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
            val luminance = rawLuminance.pow(0.35f) // Even stronger boost
            
            val rampIndex = (luminance * (asciiRamp.length - 1)).toInt()

            chars[i] = asciiRamp[rampIndex.coerceIn(0, asciiRamp.length - 1)]
            colors[i] = pixel
        }
        scaled.recycle()
    }
}
