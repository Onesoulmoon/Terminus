package com.necroware.terminusplayer.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.util.toHoursMinutes
import kotlinx.coroutines.delay

private data class DeviceReadout(val ramUsedGb: Float, val ramTotalGb: Float, val batteryTempC: Float?)

/**
 * Neofetch-inspired card: a small ASCII "terminal screen" glyph on the
 * left (safe, monospace-consistent — every line is the same character
 * count so it can't misalign), plus RAM usage and battery temperature
 * (a reasonable device-temperature proxy without needing any special
 * thermal API) on the right, instead of static device/Android-version
 * text.
 */
@Composable
fun SystemInfoCard(
    trackCount: Int,
    likedCount: Int,
    weekPlays: Int,
    weekMsPlayed: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val readout by produceState(initialValue = DeviceReadout(0f, 0f, null), context) {
        while (true) {
            value = readDeviceInfo(context)
            delay(4000)
        }
    }

    TerminalBorder(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ASCII_LOGO,
                fontFamily = FontFamily.Monospace,
                lineHeight = MaterialTheme.typography.titleLarge.fontSize,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                InfoLine("ram", "%.1f/%.1f GB".format(readout.ramUsedGb, readout.ramTotalGb))
                InfoLine("temp", readout.batteryTempC?.let { "%.1f°C".format(it) } ?: "n/a")
                InfoLine("tracks", trackCount.toString())
                InfoLine("liked", likedCount.toString())
                InfoLine("week", "$weekPlays plays · ${weekMsPlayed.toHoursMinutes()}")
            }
        }
    }
}

private const val ASCII_LOGO = "▛▀▀▀▀▀▜\n▌ >_  ▐\n▙▄▄▄▄▄▟"

private fun readDeviceInfo(context: Context): DeviceReadout {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    activityManager?.getMemoryInfo(memInfo)
    val totalBytes = memInfo.totalMem.toFloat()
    val availBytes = memInfo.availMem.toFloat()
    val usedGb = ((totalBytes - availBytes) / (1024f * 1024f * 1024f))
    val totalGb = totalBytes / (1024f * 1024f * 1024f)

    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    val tempC = if (tempTenths >= 0) tempTenths / 10f else null

    return DeviceReadout(ramUsedGb = usedGb, ramTotalGb = totalGb, batteryTempC = tempC)
}

@Composable
private fun InfoLine(key: String, value: String) {
    Row {
        Text(
            text = "$key: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
