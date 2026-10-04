package com.necroware.terminusplayer.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import com.necroware.terminusplayer.util.toHoursMinutes
import kotlinx.coroutines.delay

private data class DeviceReadout(val ramUsedGb: Float, val ramTotalGb: Float, val batteryTempC: Float?)

@Composable
fun SystemInfoCard(
    trackCount: Int,
    likedCount: Int,
    weekPlays: Int,
    weekMsPlayed: Long,
    topArtist: String = "Yeat",
    metricLogText: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalTerminalPalette.current
    val readout by produceState(initialValue = DeviceReadout(0f, 0f, null), context) {
        while (true) {
            value = readDeviceInfo(context)
            delay(4000)
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.padding(end = 20.dp).size(60.dp, 52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▛▀▀▀▀▀▜", fontFamily = TerminalFontFamily, fontSize = 14.sp, color = palette.primaryAccent, lineHeight = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("▌ ", fontFamily = TerminalFontFamily, fontSize = 14.sp, color = palette.primaryAccent, lineHeight = 14.sp)
                            AsciiMusicHeader(fontSize = 14.sp, showBrackets = false, color = palette.secondaryAccent)
                            Text("  ▐", fontFamily = TerminalFontFamily, fontSize = 14.sp, color = palette.primaryAccent, lineHeight = 14.sp)
                        }
                        Text("▙▄▄▄▄▄▟", fontFamily = TerminalFontFamily, fontSize = 14.sp, color = palette.primaryAccent, lineHeight = 14.sp)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    InfoLine("ram", "%.1f/%.1f GB".format(readout.ramUsedGb, readout.ramTotalGb))
                    InfoLine("temp", readout.batteryTempC?.let { "%.1f°C".format(it) } ?: "n/a")
                    InfoLine("tracks", trackCount.toString())
                    InfoLine("liked", likedCount.toString())
                    InfoLine("week", "$weekPlays plays · ${weekMsPlayed.toHoursMinutes()}")
                }
            }
        }

        // Dynamic Terminal Metric Log Box matching reference image 2 and user requirements
        TerminalMetricLogCard(topArtist = topArtist, weekPlays = weekPlays, logText = metricLogText)
    }
}

@Composable
fun TerminalMetricLogCard(
    topArtist: String = "Yeat",
    weekPlays: Int = 9,
    logText: String = "",
    modifier: Modifier = Modifier
) {
    val palette = LocalTerminalPalette.current
    val textToShow = logText.ifBlank {
        "METRIC // TOP_ARTIST: '$topArtist' leading the log tracking matrix index with $weekPlays recorded playback cycles this week."
    }

    TerminalBorder(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = textToShow,
                color = palette.tertiaryAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        }
    }
}

private fun readDeviceInfo(context: Context): DeviceReadout {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
    val memInfo = android.app.ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memInfo)
    val totalBytes = memInfo.totalMem.toFloat()
    val availBytes = memInfo.availMem.toFloat()
    val usedGb = ((totalBytes - availBytes) / (1024f * 1024f * 1024f))
    val totalGb = totalBytes / (1024f * 1024f * 1024f)

    val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
    val tempTenths = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    val tempC = if (tempTenths >= 0) tempTenths / 10f else null

    return DeviceReadout(ramUsedGb = usedGb, ramTotalGb = totalGb, batteryTempC = tempC)
}

@Composable
private fun InfoLine(key: String, value: String) {
    val palette = LocalTerminalPalette.current
    Row {
        Text(
            text = "$key: ",
            style = MaterialTheme.typography.bodySmall,
            color = palette.primaryAccent
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
