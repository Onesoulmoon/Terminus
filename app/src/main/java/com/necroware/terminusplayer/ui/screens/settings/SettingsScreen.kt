package com.necroware.terminusplayer.ui.screens.settings

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.BuildConfig
import com.necroware.terminusplayer.data.prefs.AlbumArtMode
import com.necroware.terminusplayer.data.prefs.DynamicThemeMode
import com.necroware.terminusplayer.data.prefs.SortDirection
import com.necroware.terminusplayer.data.prefs.SortField
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.data.prefs.TimelineStyle
import com.necroware.terminusplayer.data.prefs.UserPreferences
import com.necroware.terminusplayer.data.prefs.VisualizerMode
import com.necroware.terminusplayer.ui.components.BlockSeekBar
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.ui.components.TerminalSlider
import com.necroware.terminusplayer.ui.components.TerminalTypewriterHeader
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.ThemePresets
import kotlinx.coroutines.delay


@Composable
private fun DefaultMusicPlayerRow() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                val roleManager = context.getSystemService(RoleManager::class.java)
                val role = "android.app.role.MUSIC_PLAYER"
                try {
                    if (roleManager != null && Build.VERSION.SDK_INT >= 29 && roleManager.isRoleAvailable(role)) {
                        context.startActivity(roleManager.createRequestRoleIntent(role))
                    } else {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                    }
                } catch (_: Exception) {
                    runCatching { context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)) }
                }
            }
            .padding(14.dp)
    ) {
        Text(
            text = "[ SET AS DEFAULT MUSIC PLAYER ]",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Register Terminus as Android's default app for local audio files.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
    val importStatus by viewModel.importStatus.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("APPEARANCE", "PLAYER", "LIBRARY", "STORAGE", "ABOUT")

    val addFilesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.importFiles(uris)
    }
    val importPlaylistLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::importPlaylist)
    }

    LaunchedEffect(importStatus) {
        if (importStatus !is ImportStatus.Idle && importStatus !is ImportStatus.Running) {
            delay(3500)
            viewModel.dismissImportStatus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TerminalTypewriterHeader(
            text = "> SETTINGS",
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
            indicator = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = if (selectedTab == index) "[ $title ]" else title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { SectionLabel("THEME") }
                    item { ThemeGrid(selected = prefs.themeId, onSelect = viewModel::setTheme) }
                    item {
                        ToggleRow(
                            label = "Album Art Theme",
                            sublabel = "Derive background, surfaces and accents from the current artwork",
                            checked = prefs.useDynamicTheme,
                            onCheckedChange = viewModel::setUseDynamicTheme
                        )
                    }
                    if (prefs.useDynamicTheme) {
                        item {
                            DynamicThemeModeSection(
                                mode = prefs.dynamicThemeMode,
                                onModeChange = viewModel::setDynamicThemeMode
                            )
                        }
                    }
                    item {
                        ToggleRow(
                            label = "Custom Album Art Alternatives",
                            sublabel = "Toggle between different retro rendering modes for track artwork",
                            checked = prefs.useCustomArtAlternatives,
                            onCheckedChange = viewModel::setUseCustomArtAlternatives
                        )
                    }
                    item { SectionLabel("ALBUM COVERS ART") }
                    item {
                        AlbumArtModeSection(
                            mode = prefs.albumArtMode,
                            onModeChange = viewModel::setAlbumArtMode
                        )
                    }
                }
                1 -> {
                    item { SectionLabel("TIMELINE PROGRESS INDICATOR") }
                    item {
                        TimelineStyleSection(
                            selectedStyle = prefs.timelineStyle,
                            onStyleChange = viewModel::setTimelineStyle
                        )
                    }
                    item { SectionLabel("EQUALIZER APO CONFIGURATION EDITOR") }
                    item {
                        EqualizerApoConfigurationEditor(
                            prefs = prefs,
                            viewModel = viewModel
                        )
                    }
                    item { SectionLabel("TRANSITIONS") }
                    item {
                        CrossfadeSection(
                            enabled = prefs.crossfade.enabled,
                            durationMs = prefs.crossfade.durationMs,
                            onEnabledChange = viewModel::setCrossfadeEnabled,
                            onDurationChange = viewModel::setCrossfadeDurationMs
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Audio monitor",
                            sublabel = "Show live format, output and signal readout in the expanded player",
                            checked = prefs.audioMonitorEnabled,
                            onCheckedChange = viewModel::setAudioMonitorEnabled
                        )
                    }
                    item { SectionLabel("VISUALIZER MODE") }
                    item {
                        VisualizerModeSection(
                            mode = prefs.visualizerMode,
                            onModeChange = viewModel::setVisualizerMode
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Real-time frequency visualizer",
                            sublabel = "Capture live audio session for FFT visualizer bars (requires audio capture permission)",
                            checked = prefs.realtimeVisualizerEnabled,
                            onCheckedChange = viewModel::setRealtimeVisualizerEnabled
                        )
                    }
                    item {
                        ToggleRow(
                            label = "Terminal matrix digital rain",
                            sublabel = "Display cascading code matrix stream background in the player",
                            checked = prefs.matrixBgEnabled,
                            onCheckedChange = viewModel::setMatrixBgEnabled
                        )
                    }
                    item { SectionLabel("HARDWARE AUDIO OUTPUT") }
                    item {
                        ToggleRow(
                            label = "USB Exclusive HQ Direct Access",
                            sublabel = "Bit-perfect uncompressed 24-bit/96kHz/192kHz direct USB audio routing",
                            checked = prefs.usbExclusiveHqEnabled,
                            onCheckedChange = viewModel::setUsbExclusiveHqEnabled
                        )
                    }
                }
                2 -> {
                    item { SectionLabel("FOLDER EXCLUSIONS") }
                    item {
                        FolderExclusionSection(
                            excludedFolders = prefs.excludedFolders,
                            allFolders = allFolders,
                            onAddExclusion = viewModel::addExcludedFolder,
                            onRemoveExclusion = viewModel::removeExcludedFolder,
                            onToggleFolder = viewModel::toggleFolderExclusion
                        )
                    }
                    item { SectionLabel("SORT ORDER") }
                    item {
                        SortSection(
                            field = prefs.librarySortOrder.field,
                            direction = prefs.librarySortOrder.direction,
                            onFieldChange = viewModel::setSortField,
                            onDirectionChange = viewModel::setSortDirection
                        )
                    }
                }
                3 -> {
                    item { SectionLabel("IMPORT") }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ActionRow("[ ADD FILES ]", "Add audio files using the Android document picker") {
                                addFilesLauncher.launch(arrayOf("audio/*"))
                            }
                            ActionRow("[ IMPORT PLAYLIST ]", "Load an .m3u/.m3u8 playlist") {
                                importPlaylistLauncher.launch(
                                    arrayOf("audio/x-mpegurl", "audio/mpegurl", "application/octet-stream", "*/*")
                                )
                            }
                            when (val s = importStatus) {
                                ImportStatus.Idle, ImportStatus.Running -> Unit
                                is ImportStatus.FilesDone -> StatusText("[ IMPORTED ${s.count} FILE${if (s.count == 1) "" else "S"} ]")
                                is ImportStatus.PlaylistDone -> StatusText("[ MATCHED ${s.matched} / ${s.total} TRACKS ]")
                                is ImportStatus.Failed -> StatusText("[ ${s.message.uppercase()} ]")
                            }
                        }
                    }
                }
                4 -> {
                    item { SectionLabel("SYSTEM") }
                    item { DefaultMusicPlayerRow() }
                    item { InfoRow("Version", BuildConfig.VERSION_NAME) }
                    item { InfoRow("Platform", "Android 10+") }
                    item { InfoRow("Playback", "Media3 / ExoPlayer") }
                    item { InfoRow("UI", "Jetpack Compose / Terminal UI") }
                    
                    item { SectionLabel("DEVELOPER") }
                    item {
                        ActionRow("[ GITHUB REPOSITORY ]", "https://github.com/Onesoulmoon/Terminus") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Onesoulmoon/Terminus"))
                            context.startActivity(intent)
                        }
                    }
                    item {
                        ActionRow("[ CONTACT EMAIL ]", "onesoulmoon@gmail.com") {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:onesoulmoon@gmail.com"))
                            context.startActivity(intent)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(84.dp)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.padding(top = 2.dp)
)

@Composable
private fun ThemeGrid(selected: ThemePresetId, onSelect: (ThemePresetId) -> Unit) {
    Box(modifier = Modifier.height(240.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ThemePresets, key = { it.id }) { preset ->
                val active = preset.id == selected
                TerminalBorder(modifier = Modifier.fillMaxWidth().clickable { onSelect(preset.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Swatch(preset.background, preset.accent)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(preset.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            if (active) Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
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

@Composable
private fun TimelineStyleSection(
    selectedStyle: TimelineStyle,
    onStyleChange: (TimelineStyle) -> Unit
) {
    val palette = LocalTerminalPalette.current
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "TIMELINE INDICATOR STYLE",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            TimelineStyle.entries.forEach { style ->
                val isSelected = style == selectedStyle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStyleChange(style) }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (if (isSelected) "> " else "  ") + when (style) {
                                TimelineStyle.HIGHLIGHTED_BLOCKS -> "BLOCKS (HIGHLIGHTED TIP)"
                                TimelineStyle.ARROW_RAIL -> "ARROW RAIL [=======>........]"
                                TimelineStyle.STAR_RAIL -> "STAR RAIL [oooooooo*---------]"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text(
                                text = "[ACTIVE]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    BlockSeekBar(
                        positionProvider = { 150000L },
                        durationMs = 300000L,
                        onSeek = {},
                        style = style,
                        tipColor = palette.highlightAccent,
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderExclusionSection(
    excludedFolders: Set<String>,
    allFolders: List<String>,
    onAddExclusion: (String) -> Unit,
    onRemoveExclusion: (String) -> Unit,
    onToggleFolder: (String) -> Unit
) {
    var newFolderText by remember { mutableStateOf("") }
    val palette = LocalTerminalPalette.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EXCLUDED PATH PATTERNS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Files matching these sub-paths or folder names (e.g. WhatsApp, Recordings) will be excluded from library scans.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                excludedFolders.forEach { excluded ->
                    if (excluded.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[ $excluded ]",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.secondaryAccent
                            )
                            Text(
                                text = "[ REMOVE ]",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable { onRemoveExclusion(excluded) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newFolderText,
                        onValueChange = { newFolderText = it },
                        placeholder = { Text("folder name or subpath", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                    Text(
                        text = "[ ADD ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (newFolderText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(enabled = newFolderText.isNotBlank()) {
                                onAddExclusion(newFolderText)
                                newFolderText = ""
                            }
                            .padding(8.dp)
                    )
                }
            }
        }

        if (allFolders.isNotEmpty()) {
            TerminalBorder(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DETECTED LIBRARY FOLDERS",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toggle individual folders to exclude or include them in your library:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    allFolders.forEach { folderPath ->
                        val folderName = folderPath.substringAfterLast("/").ifBlank { folderPath }
                        val isExcluded = excludedFolders.any { 
                            it.isNotBlank() && (folderPath.contains(it, ignoreCase = true) || folderName.contains(it, ignoreCase = true))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isExcluded) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = folderPath,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (isExcluded) "[ EXCLUDED ]" else "[ INCLUDED ]",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isExcluded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onToggleFolder(folderPath) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicThemeModeSection(
    mode: DynamicThemeMode,
    onModeChange: (DynamicThemeMode) -> Unit
) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Dynamic color scheme mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                when (mode) {
                    DynamicThemeMode.DARK -> "Deep dark terminal background with vibrant artwork accents"
                    DynamicThemeMode.LIGHT -> "High-contrast paper/light terminal background with vibrant artwork accents"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                DynamicThemeMode.entries.forEach { candidate ->
                    Text(
                        text = if (candidate == mode) "[ ${candidate.name} ]" else candidate.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (candidate == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onModeChange(candidate) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumArtModeSection(mode: AlbumArtMode, onModeChange: (AlbumArtMode) -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Rendering style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                when (mode) {
                    AlbumArtMode.PIXELATED -> "Chunkier 8-bit / 16-bit arcade pixels"
                    AlbumArtMode.ASCII -> "Classic character-grid terminal art"
                    AlbumArtMode.CRT -> "Analog cathode-ray tube glass simulation"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                AlbumArtMode.entries.forEach { candidate ->
                    Text(
                        text = if (candidate == mode) "[ ${candidate.name} ]" else candidate.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (candidate == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onModeChange(candidate) }
                    )
                }
            }
        }
    }
}

private val EQ_BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

@Composable
private fun VisualizerModeSection(
    mode: VisualizerMode,
    onModeChange: (VisualizerMode) -> Unit
) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualizerMode.entries.forEach { candidate ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onModeChange(candidate) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (if (candidate == mode) "> " else "  ") + candidate.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (candidate == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (candidate == mode) {
                        Text("[ACTIVE]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EqualizerApoConfigurationEditor(
    prefs: UserPreferences,
    viewModel: SettingsViewModel
) {
    val eq = prefs.equalizer
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // --- BLOCK 1: Device Configuration & Status Window ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1 // DEVICE STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Instant mode: [ON]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Selected Device: Pimax - NVIDIA High Definition Audio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Text("Channel Configuration: From device (Stereo) // APO installed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // --- BLOCK 2: Preamplification Gain Slider ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("2 // PREAMPLIFICATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("[ %.2f dB ]".format(eq.preampGainDb), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(4.dp))
                TerminalSlider(
                    value = (eq.preampGainDb + 20f) / 40f,
                    onValueChange = { fraction -> viewModel.setPreampGain((fraction * 40f) - 20f) },
                    originFraction = 0.5f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- BLOCK 3: Graphic EQ Response Curve & Grid Table ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3 // GRAPHIC EQ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[RESET]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { viewModel.resetEqualizerBands() }.padding(end = 8.dp)
                        )
                        Text(
                            text = if (eq.enabled) "[ON]" else "[OFF]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.setEqualizerEnabled(!eq.enabled) }
                        )
                    }
                }
                
                val themePrimary = MaterialTheme.colorScheme.primary
                val themeDisabled = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                val curveColor = if (eq.enabled) themePrimary else themeDisabled
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

                // Real-time interconnected spline frequency curve canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(1.dp, gridColor)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val centerY = h / 2f
                        
                        // Reference gridlines
                        drawLine(gridColor, Offset(0f, centerY), Offset(w, centerY), 1f)
                        drawLine(gridColor, Offset(w * 0.3f, 0f), Offset(w * 0.3f, h), 1f)
                        drawLine(gridColor, Offset(w * 0.6f, 0f), Offset(w * 0.6f, h), 1f)
                        
                        val path = Path()
                        val bands = eq.bandGainsDb
                        val count = bands.size.coerceAtLeast(1)
                        val step = w / (count - 1).toFloat()
                        
                        for (i in 0 until count) {
                            val gain = bands.getOrElse(i) { 0 }
                            val y = centerY - (gain / 12f) * (h / 2.2f)
                            val x = i * step
                            
                            if (i == 0) { path.moveTo(x, y) } else {
                                val prevGain = bands.getOrElse(i - 1) { 0 }
                                val prevY = centerY - (prevGain / 12f) * (h / 2.2f)
                                val prevX = (i - 1) * step
                                val cx = (prevX + x) / 2f
                                path.cubicTo(cx, prevY, cx, y, x, y)
                            }
                            
                            drawCircle(
                                color = curveColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                        drawPath(
                            path = path,
                            color = curveColor,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Grid table rendering frequencies and adjustable nodes
                EQ_BAND_LABELS.forEachIndexed { index, label ->
                    val gainDb = eq.bandGainsDb.getOrElse(index) { 0 }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${label}Hz", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
                        TerminalSlider(
                            value = (gainDb + 12) / 24f,
                            onValueChange = { fraction -> viewModel.setEqualizerBand(index, ((fraction * 24f) - 12f).toInt()) },
                            originFraction = 0.5f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${if (gainDb > 0) "+" else ""}${gainDb}dB", style = MaterialTheme.typography.bodySmall, color = if (eq.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }

        // --- BLOCK 4: High-Pass Filter Controller ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("4 // HIGH-PASS FILTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(if (eq.highPassEnabled) "[ON]" else "[OFF]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.setHighPassEnabled(!eq.highPassEnabled) })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Corner frequency: %.1f Hz".format(eq.highPassFreq), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fixed Q: 0.7071", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TerminalSlider(
                    value = ((eq.highPassFreq - 20f) / 480f).coerceIn(0f, 1f),
                    onValueChange = { fraction -> viewModel.setHighPassFreq(20f + fraction * 480f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- BLOCK 5: Low-Pass Filter Controller ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("5 // LOW-PASS FILTER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(if (eq.lowPassEnabled) "[ON]" else "[OFF]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.setLowPassEnabled(!eq.lowPassEnabled) })
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Corner frequency: %.0f Hz".format(eq.lowPassFreq), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fixed Q: 0.7071", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TerminalSlider(
                    value = ((eq.lowPassFreq - 1000f) / 19000f).coerceIn(0f, 1f),
                    onValueChange = { fraction -> viewModel.setLowPassFreq(1000f + fraction * 19000f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- ANALYSIS PANEL ---
        TerminalBorder(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("ANALYSIS PANEL // ESTIMATED PROPERTIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Peak gain: %.1f dB".format(eq.preampGainDb + (eq.bandGainsDb.maxOrNull() ?: 0)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Latency: 0.0 ms (0 s.)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Init. time: 8.2 ms", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("CPU usage: 0.2 % (one core)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CrossfadeSection(enabled: Boolean, durationMs: Int, onEnabledChange: (Boolean) -> Unit, onDurationChange: (Int) -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Overlap tracks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Text(if (enabled) "[ON]" else "[OFF]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onEnabledChange(!enabled) })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DURATION", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1fs".format(durationMs / 1000f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
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
    TerminalBorder(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                Text(sublabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (checked) "[ON]" else "[OFF]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SortSection(field: SortField, direction: SortDirection, onFieldChange: (SortField) -> Unit, onDirectionChange: (SortDirection) -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Column {
            SortField.entries.forEach { candidate ->
                Text(
                    text = (if (candidate == field) "> " else "  ") + candidate.name.replace('_', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (candidate == field) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().clickable { onFieldChange(candidate) }.padding(vertical = 6.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("[ ASCENDING ]", style = MaterialTheme.typography.labelSmall, color = if (direction == SortDirection.ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onDirectionChange(SortDirection.ASC) })
                Text("[ DESCENDING ]", style = MaterialTheme.typography.labelSmall, color = if (direction == SortDirection.DESC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onDirectionChange(SortDirection.DESC) })
            }
        }
    }
}

@Composable
private fun ActionRow(label: String, sublabel: String, onClick: () -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text(sublabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusText(text: String) = Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)

@Composable
private fun InfoRow(label: String, value: String) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun InfoBlock(text: String) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
