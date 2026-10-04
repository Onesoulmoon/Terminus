package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlin.math.abs

@Composable
fun SignalHud(
    codec: String,
    sampleRate: String,
    spectrumData: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.6f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPlaying) "●" else "○",
                    color = if (isPlaying) accent else Color.Gray,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "SIGNAL // LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = TerminalFontFamily
                )
            }
            Text(
                text = "${codec.uppercase()}  ${sampleRate.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = TerminalFontFamily
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(top = 8.dp)
        ) {
            val bars = 64
            val gap = size.width / bars
            val numBands = spectrumData.size
            
            for (i in 0 until bars) {
                // Map the 64 visual bars to the 16 DSP bands with linear interpolation for smoothness
                val bandProgress = i.toFloat() / bars * (numBands - 1)
                val lowerBand = bandProgress.toInt()
                val upperBand = (lowerBand + 1).coerceAtMost(numBands - 1)
                val fraction = bandProgress - lowerBand
                
                val lowerMag = if (isPlaying) spectrumData[lowerBand] else 0.05f
                val upperMag = if (isPlaying) spectrumData[upperBand] else 0.05f
                val magnitude = lowerMag + (upperMag - lowerMag) * fraction
                
                val heightPercent = magnitude.coerceIn(0.05f, 1f)
                
                val left = i * gap
                val barHeight = size.height * heightPercent
                drawRect(
                    color = accent.copy(alpha = if (isPlaying) 0.8f else 0.2f),
                    topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(gap * 0.7f, barHeight)
                )
            }
        }
    }
}
