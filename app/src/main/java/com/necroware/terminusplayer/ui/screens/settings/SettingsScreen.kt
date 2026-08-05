package com.necroware.terminusplayer.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.prefs.SortDirection
import com.necroware.terminusplayer.data.prefs.SortField
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.ui.components.TerminalSlider
import com.necroware.terminusplayer.ui.theme.ThemePresets
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val importStatus by viewModel.importStatus.collectAsStateWithLifecycle()

    val addFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> viewModel.importFiles(uris) }

    val importPlaylistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importPlaylist) }

    // Auto-clear the status line a few seconds after a finished import so
    // it doesn't linger indefinitely as stale state.
    LaunchedEffect(importStatus) {
        if (importStatus !is ImportStatus.Idle && importStatus !is ImportStatus.Running) {
            delay(4000)
            viewModel.dismissImportStatus()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "> SETTINGS_",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item { SectionLabel("THEME") }
        item { ThemeGrid(selected = prefs.themeId, onSelect = viewModel::setTheme) }

        item { SectionLabel("EQUALIZER") }
        item {
            EqualizerSection(
                enabled = prefs.equalizer.enabled,
                bandGains = prefs.equalizer.bandGainsDb,
                onEnabledChange = viewModel::setEqualizerEnabled,
                onBandChange = viewModel::setEqualizerBand,
                onReset = viewModel::resetEqualizerBands
            )
        }

        item { SectionLabel("CROSSFADE") }
        item {
            CrossfadeSection(
                enabled = prefs.crossfade.enabled,
                durationMs = prefs.crossfade.durationMs,
                onEnabledChange = viewModel::setCrossfadeEnabled,
                onDurationChange = viewModel::setCrossfadeDurationMs
            )
        }

        item { SectionLabel("CODEC") }
        item {
            ToggleRow(
                label = "Prefer hardware decoder",
                sublabel = "Falls back to software automatically if unsupported",
                checked = prefs.preferHardwareDecoder,
                onCheckedChange = viewModel::setPreferHardwareDecoder
            )
        }

        item { SectionLabel("LIBRARY SORT") }
        item {
            SortSection(
                field = prefs.librarySortOrder.field,
                direction = prefs.librarySortOrder.direction,
                onFieldChange = viewModel::setSortField,
                onDirectionChange = viewModel::setSortDirection
            )
        }

        item { SectionLabel("IMPORT") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionRow(
                    label = "[ ADD FILES ]",
                    sublabel = "Copy audio files into your library"
                ) { addFilesLauncher.launch(arrayOf("audio/*")) }
                ActionRow(
                    label = "[ IMPORT PLAYLIST ]",
                    sublabel = "Load an .m3u/.m3u8 playlist"
                ) {
                    importPlaylistLauncher.launch(
                        arrayOf("audio/x-mpegurl", "audio/mpegurl", "application/octet-stream", "*/*")
                    )
                }

                val statusText = when (val s = importStatus) {
                    ImportStatus.Idle -> null
                    ImportStatus.Running -> "[ working... ]"
                    is ImportStatus.FilesDone -> "[ imported ${s.count} file${if (s.count == 1) "" else "s"} ]"
                    is ImportStatus.PlaylistDone -> "[ matched ${s.matched} / ${s.total} tracks ]"
                    is ImportStatus.Failed -> "[ ${s.message} ]"
                }
                statusText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ThemeGrid(selected: ThemePresetId, onSelect: (ThemePresetId) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(ThemePresets, key = { it.id }) { preset ->
            val isSelected = preset.id == selected
            TerminalBorder(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(preset.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Swatch(preset.background, preset.accent)
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSelected) "ACTIVE" else " ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Swatch(background: Color, accent: Color) {
    Row(modifier = Modifier.size(24.dp).clip(MaterialTheme.shapes.extraSmall)) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(background))
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(accent))
    }
}

private val EQ_BAND_LABELS = listOf("60", "230", "910", "3.6k", "14k")

@Composable
private fun EqualizerSection(
    enabled: Boolean,
    bandGains: List<Int>,
    onEnabledChange: (Boolean) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onReset: () -> Unit
) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "5-BAND EQ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    Text(
                        text = "[RESET] ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onReset() }
                    )
                    Text(
                        text = if (enabled) "[ON]" else "[OFF]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onEnabledChange(!enabled) }
                    )
                }
            }

            EQ_BAND_LABELS.forEachIndexed { index, label ->
                val gainDb = bandGains.getOrElse(index) { 0 }
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${label}Hz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${if (gainDb > 0) "+" else ""}${gainDb}dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TerminalSlider(
                        value = (gainDb + 12) / 24f,
                        onValueChange = { fraction -> onBandChange(index, ((fraction * 24f) - 12f).toInt()) },
                        originFraction = 0.5f,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CrossfadeSection(
    enabled: Boolean,
    durationMs: Int,
    onEnabledChange: (Boolean) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fade between tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (enabled) "[ON]" else "[OFF]",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onEnabledChange(!enabled) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DURATION",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.1fs".format(durationMs / 1000f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TerminalSlider(
                value = ((durationMs - 1000) / 11000f).coerceIn(0f, 1f),
                onValueChange = { fraction -> onDurationChange((1000 + fraction * 11000).toInt()) },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, sublabel: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (checked) "[ON]" else "[OFF]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SortSection(
    field: SortField,
    direction: SortDirection,
    onFieldChange: (SortField) -> Unit,
    onDirectionChange: (SortDirection) -> Unit
) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            SortField.entries.forEach { candidate ->
                Text(
                    text = (if (candidate == field) "> " else "  ") + candidate.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (candidate == field) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFieldChange(candidate) }
                        .padding(vertical = 6.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "[ ASCENDING ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (direction == SortDirection.ASC) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.clickable { onDirectionChange(SortDirection.ASC) }
                )
                Text(
                    text = "[ DESCENDING ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (direction == SortDirection.DESC) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.clickable { onDirectionChange(SortDirection.DESC) }
                )
            }
        }
    }
}

@Composable
private fun ActionRow(label: String, sublabel: String, onClick: () -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text(
                sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
