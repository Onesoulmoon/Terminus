package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.ui.theme.LocalTerminusAccent

@Composable
fun AsciiVisualizer(
    isPlaying: Boolean,
    barCount: Int = 4,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalTerminusAccent.current
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ascii_bar")
    val animProgresses = List(barCount) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(280 + i * 110, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
    }

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val barHeight = if (isPlaying) animProgresses[i].value else 0.25f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * barHeight).dp)
                    .background(accentColor)
            )
        }
    }
}
