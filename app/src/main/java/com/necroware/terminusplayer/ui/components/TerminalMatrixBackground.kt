package com.necroware.terminusplayer.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun TerminalMatrixBackground(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val characters = remember { "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZﾊﾐﾋｰｳｼﾅﾓﾆｻﾜﾂｵﾘｱﾎﾏｹﾒｴｶｷﾑﾕﾗｾﾈｽﾀﾇﾍ".toCharArray() }
    var columns by remember { mutableStateOf<List<MatrixColumn>>(emptyList()) }

    val paint = remember(accentColor) {
        Paint().apply {
            color = accentColor.copy(alpha = 0.08f).toArgb()
            textSize = 48f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
    }

    val leadPaint = remember(accentColor) {
        Paint().apply {
            color = accentColor.copy(alpha = 0.25f).toArgb()
            textSize = 48f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis { _ ->
                columns = columns.map { col ->
                    var newY = col.y + col.speed
                    var newChars = col.chars
                    if (newY > 3500f) { // Safely allow flowing far below large displays
                        newY = Random.nextFloat() * -400f
                        col.speed = Random.nextFloat() * 7f + 4f
                    }
                    if (Random.nextFloat() < 0.04f) {
                        newChars = col.chars.toMutableList().apply {
                            val idx = Random.nextInt(size)
                            this[idx] = characters[Random.nextInt(characters.size)]
                        }
                    }
                    col.copy(y = newY, chars = newChars)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawWithCache {
                val width = size.width
                val height = size.height
                val fontSize = 48f
                val numCols = (width / (fontSize * 0.75f)).toInt().coerceAtLeast(1)

                if (columns.size != numCols) {
                    columns = List(numCols) {
                        MatrixColumn(
                            y = Random.nextFloat() * -height * 1.5f,
                            speed = Random.nextFloat() * 7f + 4f,
                            chars = List(32) { characters[Random.nextInt(characters.size)] }
                        )
                    }
                }

                onDrawWithContent {
                    drawIntoCanvas { canvas ->
                        columns.forEachIndexed { colIdx, column ->
                            val x = colIdx * fontSize * 0.75f + (fontSize * 0.38f)
                            
                            column.chars.forEachIndexed { charIdx, char ->
                                val charY = column.y - (charIdx * fontSize)
                                val adjustedY = if (charY > height + 400f) -100f else charY
                                if (adjustedY in 0f..(height + 400f)) {
                                    val currentPaint = if (charIdx == 0) leadPaint else paint
                                    canvas.nativeCanvas.drawText(char.toString(), x, adjustedY, currentPaint)
                                }
                            }
                        }
                    }
                }
            }
    )
}

private data class MatrixColumn(
    val y: Float,
    var speed: Float,
    val chars: List<Char>
)
