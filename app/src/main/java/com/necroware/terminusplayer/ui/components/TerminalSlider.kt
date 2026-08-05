package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/**
 * Same segmented-block visual language as [BlockSeekBar] but for an
 * arbitrary 0f..1f value (EQ gain, crossfade duration, etc.) rather than
 * playback position. [originFraction] draws the "center" tick differently
 * (used for the EQ bands, which are bipolar around 0dB) — leave it null
 * for a plain fill-from-left control.
 */
@Composable
fun TerminalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    segmentCount: Int = 20,
    originFraction: Float? = null
) {
    val clamped = value.coerceIn(0f, 1f)
    var barWidthPx by remember { mutableStateOf(1f) }

    fun updateFromOffsetX(x: Float) {
        onValueChange((x / barWidthPx).coerceIn(0f, 1f))
    }

    val filledSegments = (clamped * segmentCount).toInt().coerceIn(0, segmentCount)
    val originSegment = originFraction?.let { (it * segmentCount).toInt().coerceIn(0, segmentCount - 1) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                detectTapGestures { offset -> updateFromOffsetX(offset.x) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ -> updateFromOffsetX(change.position.x) }
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(segmentCount) { index ->
            val isFilled = if (originSegment != null && originFraction != null) {
                if (clamped >= originFraction) index in originSegment..filledSegments.coerceAtLeast(originSegment)
                else index in filledSegments.coerceAtMost(originSegment)..originSegment
            } else {
                index < filledSegments
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(if (originSegment == index) 18.dp else 12.dp)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}
