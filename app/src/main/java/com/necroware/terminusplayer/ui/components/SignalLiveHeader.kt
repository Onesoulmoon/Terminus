package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

@Composable
fun SignalLiveHeader(
    amplitudesProvider: () -> FloatArray,
    modifier: Modifier = Modifier,
    formatText: String = "MP3  44.1 kHz",
    terminalColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "signal")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF050505)) // Pure technical black
            .border(1.dp, Color(0xFF1A1A1A))
            .padding(10.dp)
    ) {
        // ...
        // Header Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "● ",
                    color = terminalColor.copy(alpha = dotAlpha),
                    fontSize = 10.sp
                )
                Text(
                    text = "SIGNAL // LIVE",
                    color = terminalColor,
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = formatText,
                color = terminalColor.copy(alpha = 0.7f),
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // High-Performance Symmetrical Waveform Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            val amplitudes = amplitudesProvider()
            val barCount = amplitudes.size
            val barWidth = 3.dp.toPx()
            val gap = if (barCount > 1) (size.width - (barCount * barWidth)) / (barCount - 1) else 0f
            val centerY = size.height / 2f

            for (i in 0 until barCount) {
                val amp = amplitudes[i].coerceIn(0f, 1f)
                val barHeight = (size.height * 0.9f) * amp
                val x = i * (barWidth + gap)

                // Ensure a minimum height for visibility as "dots" when quiet
                val finalHeight = barHeight.coerceAtLeast(2.dp.toPx())

                // Draw Symmetrical Top-and-Bottom vertical bar
                drawRect(
                    color = terminalColor,
                    topLeft = Offset(x, centerY - (finalHeight / 2f)),
                    size = Size(barWidth, finalHeight)
                )
            }
        }
    }
}
