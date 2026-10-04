package com.necroware.terminusplayer.ui.components

import android.content.Context
import android.media.AudioManager
import android.os.Process
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay

// Tactical Arcade CRT Palette
val TacticalCardBg = Color(0xFFD2DAD4)        // Inverted screen white
val TacticalInkDark = Color(0xFF0A0E0C)       // Heavy black text
val TacticalNeonCyan = Color(0xFF00FFC2)     // Bright status highlight
val TacticalNeonPink = Color(0xFFFF0055)     // Active indicator tag

private data class TacticalDeviceReadout(val audioDevice: String, val cpuPercent: Int)

@Composable
fun TacticalAudioMonitorCard(
    codecLabel: String,
    bitDepthLabel: String,
    kbpsLabel: String,
    sizeLabel: String,
    isLossless: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TacticalCardBg,
    contentColor: Color = TacticalInkDark,
    accentColor: Color = TacticalNeonPink
) {
    val context = LocalContext.current
    val terminalAccent = MaterialTheme.colorScheme.primary

    val volumePercent by produceState(initialValue = 0, context) {
        while (true) {
            value = readVolumePercent(context)
            delay(2000)
        }
    }

    val deviceReadout by produceState(initialValue = TacticalDeviceReadout("—", 0), context) {
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
            value = TacticalDeviceReadout(audioOutputDeviceLabel(context), percent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(2.dp, Color.Black)
            .padding(4.dp)
    ) {
        // --- TOP HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (accentColor == TacticalNeonPink) terminalAccent else accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AUDIO MONITOR // TELEMETRY",
                    color = Color.White,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // Neon Active Badge
            Box(
                modifier = Modifier
                    .background(if (isLossless) TacticalNeonCyan else if (accentColor == TacticalNeonPink) terminalAccent else accentColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isLossless) "LOSSLESS" else "LOSSY",
                    color = Color.Black,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        // --- MAIN TELEMETRY CONTENT SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Format & Bit Depth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TacticalInfoText("fmt: $codecLabel", color = contentColor)
                TacticalInfoText("bit: $bitDepthLabel", color = contentColor)
            }

            // Row 2: Rate & Bitrate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TacticalInfoText("kbps: $kbpsLabel", color = contentColor)
                TacticalInfoText("size: $sizeLabel", color = contentColor)
            }

            // Row 3: Output Device
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TacticalInfoText("out: ${deviceReadout.audioDevice}", color = contentColor)
            }

            // Divider Line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(1.dp)
            ) {
                drawLine(
                    color = contentColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            }

            // Row 4: CPU & Volume Block Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalInfoText("CPU: ${deviceReadout.cpuPercent}%", color = contentColor, isBold = true)

                // Blocky ASCII Volume Display [ ██░░░░░░ ]
                val totalBlocks = 8
                val filledBlocks = ((volumePercent / 100f) * totalBlocks).toInt().coerceIn(0, totalBlocks)
                val blockStr = "█".repeat(filledBlocks) + "░".repeat(totalBlocks - filledBlocks)

                Text(
                    text = "VOL: $volumePercent% [$blockStr]",
                    color = contentColor,
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TacticalInfoText(text: String, color: Color, isBold: Boolean = false) {
    Text(
        text = text.uppercase(),
        color = color,
        fontFamily = TerminalFontFamily,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
