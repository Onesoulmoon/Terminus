package com.necroware.terminusplayer.ui.components

import android.content.Context
import android.media.AudioManager
import android.os.Process
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import kotlinx.coroutines.delay

private data class AudioDeviceReadout(val audioDevice: String, val cpuPercent: Int)

@Composable
fun AudioMonitorCard(
    codecLabel: String,
    bitDepthLabel: String,
    kbpsLabel: String,
    sizeLabel: String,
    isLossless: Boolean,
    repeatMode: Int,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false
) {
    val context = LocalContext.current

    val volumePercent by produceState(initialValue = 0, context) {
        while (true) {
            value = readVolumePercent(context)
            delay(2000)
        }
    }

    val deviceReadout by produceState(initialValue = AudioDeviceReadout("—", 0), context) {
        var lastCpuTimeMs = Process.getElapsedCpuTime()
        var lastWallTimeMs = System.currentTimeMillis()
        while (true) {
            delay(2000)
            val cpuTimeMs = Process.getElapsedCpuTime()
            val wallTimeMs = System.currentTimeMillis()
            val cpuDelta = (cpuTimeMs - lastCpuTimeMs).coerceAtLeast(0L)
            val wallDelta = (wallTimeMs - lastWallTimeMs).coerceAtLeast(1L)
            val percent = ((cpuDelta.toFloat() / wallDelta) * 100).toInt().coerceIn(0, 100)
            lastCpuTimeMs = cpuTimeMs
            lastWallTimeMs = wallTimeMs
            value = AudioDeviceReadout(audioOutputDeviceLabel(context), percent)
        }
    }

    TerminalBorder(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AUDIO MONITOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isLossless) "LOSSLESS" else "LOSSY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            MonitorRow("fmt", codecLabel, "bit: $bitDepthLabel")
            MonitorRow("kbps", kbpsLabel, "size: $sizeLabel")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            MonitorRow("device", deviceReadout.audioDevice, "VOL $volumePercent%")
            MonitorRow("cpu", "${deviceReadout.cpuPercent}%", "")

            SimulatedSpectrum(isPlaying = isPlaying, modifier = Modifier.padding(top = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LOOP " + when (repeatMode) {
                        Player.REPEAT_MODE_ALL -> "ALL"
                        Player.REPEAT_MODE_ONE -> "ONE"
                        else -> "OFF"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SimulatedSpectrum(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 12
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val heightPercent by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (400..800).random(),
                        delayMillis = (0..200).random(),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            
            val displayHeight = if (isPlaying) heightPercent else 0.1f
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(displayHeight)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            )
        }
    }
}

@Composable
private fun MonitorRow(label: String, value: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f, fill = true)) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing.isNotBlank()) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun readVolumePercent(context: Context): Int {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return 0
    val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    return ((current.toFloat() / max) * 100).toInt()
}

@Suppress("DEPRECATION")
private fun audioOutputDeviceLabel(context: Context): String {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return "—"
    return when {
        audioManager.isBluetoothA2dpOn -> "Bluetooth"
        audioManager.isWiredHeadsetOn -> "Wired"
        else -> "Speaker"
    }
}
