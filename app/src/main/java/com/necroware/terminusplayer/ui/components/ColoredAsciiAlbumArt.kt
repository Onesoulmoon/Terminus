package com.necroware.terminusplayer.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.util.FastAsciiMatrix

@Composable
fun OptimizedAsciiAlbumArt(
    asciiMatrixProvider: () -> FastAsciiMatrix?,
    modifier: Modifier = Modifier,
    gridSize: Int = 64
) {
    // Single pre-allocated native Paint handle
    val textPaint = remember {
        Paint().apply {
            isAntiAlias = false // Sharp terminal pixels
            typeface = Typeface.MONOSPACE
            style = Paint.Style.FILL
        }
    }

    val singleCharBuffer = remember { CharArray(1) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFF09090B))
            .border(1.dp, Color(0xFF1F1F23))
            .padding(8.dp)
    ) {
        val matrix = asciiMatrixProvider() ?: return@Canvas
        val cellWidth = size.width / gridSize
        val cellHeight = size.height / gridSize
        textPaint.textSize = cellHeight

        var index = 0
        for (y in 0 until gridSize) {
            val yPos = (y + 1) * cellHeight
            for (x in 0 until gridSize) {
                textPaint.color = matrix.colors[index]
                singleCharBuffer[0] = matrix.chars[index]

                drawContext.canvas.nativeCanvas.drawText(
                    singleCharBuffer,
                    0,
                    1,
                    x * cellWidth,
                    yPos,
                    textPaint
                )
                index++
            }
        }
    }
}
