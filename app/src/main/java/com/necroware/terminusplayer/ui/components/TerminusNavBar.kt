package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.navigation.Destination
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.launch

@Composable
fun TerminusNavBar(
    items: List<Destination>,
    currentRoute: String?,
    onTabSelected: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { destination ->
            val isSelected = currentRoute == destination.route
            val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(destination) }
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (destination) {
                        Destination.Home -> HomeNavIcon(isSelected, tint) {}
                        Destination.Library -> LibraryNavIcon(isSelected, tint) {}
                        Destination.Playlists -> PlaylistNavIcon(isSelected, tint) {}
                        Destination.Download -> DownloadNavIcon(isSelected, tint) {}
                        Destination.Stats -> StatsNavIcon(isSelected, tint) {}
                        Destination.Settings -> SettingsNavIcon(isSelected, tint) {}
                        else -> {
                            Text(
                                text = destination.label.take(1),
                                color = tint,
                                fontFamily = TerminalFontFamily
                            )
                        }
                    }
                }
                Text(
                    text = destination.label,
                    color = tint,
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
fun HomeNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var trigger by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (trigger) 6f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { trigger = false },
        label = "HomeNudge"
    )
    val cursorChar = if (trigger) "-" else "_"

    LaunchedEffect(isSelected) {
        if (isSelected) trigger = true
    }

    Text(
        text = ">$cursorChar",
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { translationX = offsetX }
    )
}

@Composable
fun LibraryNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LibrarySpin"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) rotationTarget += 360f
    }

    Text(
        text = TerminusSymbols.LIBRARY,
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
fun PlaylistNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PlaylistSpin"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) rotationTarget += 360f
    }

    Text(
        text = TerminusSymbols.PLAYLIST,
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
fun StatsNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "StatsSpin"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) rotationTarget += 360f
    }

    Text(
        text = TerminusSymbols.STATS,
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
fun DownloadNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "DownloadSpin"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) rotationTarget += 360f
    }

    Text(
        text = "[↓]",
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
    )
}

@Composable
fun SettingsNavIcon(isSelected: Boolean, tint: Color, onClick: () -> Unit) {
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GearSpin"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) rotationTarget += 360f
    }

    Text(
        text = TerminusSymbols.SETTINGS,
        color = tint,
        fontFamily = TerminalFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
    )
}
