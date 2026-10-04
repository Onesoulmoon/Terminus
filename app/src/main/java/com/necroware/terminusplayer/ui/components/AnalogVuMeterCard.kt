package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TopVuMeterHeader(
    audioLevel: Float, // Normalized value from 0.0f to 1.0f
    modifier: Modifier = Modifier,
    formatLabel: String = "PCM_16BIT / 48kHz",
    accentColor: Color = LocalTerminalPalette.current.secondaryAccent
) {
    val animatedLevel by animateFloatAsState(
        targetValue = audioLevel.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "TapeVuSweep"
    )

    var reelStep by remember { mutableIntStateOf(0) }
    val reelFrames = remember { listOf("(o)", "(*)", "(+)", "(x)") }

    LaunchedEffect(audioLevel > 0.01f) {
        if (audioLevel > 0.01f) {
            while (isActive) {
                delay(120)
                reelStep = (reelStep + 1) % reelFrames.size
            }
        }
    }

    val palette = LocalTerminalPalette.current
    val warningColor = Color(0xFFFF0055)
    val isOverload = animatedLevel >= 0.88f
    val currentAccent = if (isOverload) warningColor else accentColor

    val dbValue = -40.0f + (animatedLevel * 40.5f) // Maps 0..1 to -40.0dB .. +0.5dB

    // Construct Tape Rail
    val totalRailLen = 10
    val filledCount = (animatedLevel * totalRailLen).toInt().coerceIn(0, totalRailLen)
    val leadingCount = if (filledCount in 1 until totalRailLen) 1 else 0
    val blockCount = (filledCount - leadingCount).coerceAtLeast(0)
    val emptyCount = (totalRailLen - filledCount).coerceAtLeast(0)

    val leftRail = "≡".repeat(leadingCount) + "█".repeat(blockCount) + "=".repeat(emptyCount)
    val rightRail = "=".repeat(emptyCount) + "█".repeat(blockCount) + "≡".repeat(leadingCount)

    val leftReelStr = reelFrames[reelStep % reelFrames.size]
    val rightReelStr = reelFrames[(reelStep + 1) % reelFrames.size]

    val dbCenterStr = if (isOverload) {
        "OVERLOAD"
    } else {
        "%+.1fdB".format(dbValue)
    }

    // Lower Stereo Channel L / R meters
    val channelBlocks = (animatedLevel * 6).toInt().coerceIn(0, 6)
    val lBlocks = "█".repeat(channelBlocks)
    val rBlocks = "█".repeat(channelBlocks)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(1.dp, currentAccent)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TAPE_VU_DECK [CH_01+02]",
                color = currentAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isOverload) "[ PEAK_HOLD ]" else "[ TAPE_FEED_OK ]",
                color = if (isOverload) warningColor else palette.primaryAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Tape Deck Reel & Rails Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$leftReelStr$leftRail[ $dbCenterStr ]$rightRail$rightReelStr",
                color = currentAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom L / R Format Spec Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "L: ${lBlocks.ifEmpty { " " }}",
                color = currentAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatLabel,
                color = palette.tertiaryAccent.copy(alpha = 0.85f),
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp
            )
            Text(
                text = "${rBlocks.ifEmpty { " " }} :R",
                color = currentAccent,
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
