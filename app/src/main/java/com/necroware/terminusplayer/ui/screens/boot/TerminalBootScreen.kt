package com.necroware.terminusplayer.ui.screens.boot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay

/**
 * Simulates an authentic terminal boot sequence before loading the main UI.
 */
@Composable
fun TerminalBootScreen(onBootComplete: () -> Unit) {
    val bootLogs = remember { mutableStateListOf<String>() }
    val terminalColor = MaterialTheme.colorScheme.primary

    val logoLines = listOf(
        " _____ _____ ____  __  __ ___ _   _ _   _ ____  ",
        "|_   _| ____|  _ \\|  \\/  |_ _| \\ | | | | |/ ___| ",
        "  | | |  _| | |_) | |\\/| | | | |  \\| | | | |\\___ \\ ",
        "  | | | |___|  _ <| |  | | | | | |\\  | |_| |___) |",
        "  |_| |_____|_| \\_\\_|  |_|___|_| \\_| \\___/|____/ "
    )

    val script = listOf(
        "TERMINUS BIOS v3.04 (C) 2026 NECROWARE SYS",
        "CHECKING HARDWARE RAM ... 640KB OK",
        "MOUNTING AUDIO_DSP ENGINE ... DONE",
        "LOADING CROSSFADE PIPELINE ... OK",
        "INITIALIZING HIGH-BAND EQUALIZER ... OK",
        "-----------------------------------------",
        "LOGO_PLACEHOLDER",
        "-----------------------------------------",
        "SYSTEM READY. LAUNCHING AUDIO HUD..."
    )

    LaunchedEffect(Unit) {
        for (line in script) {
            if (line == "LOGO_PLACEHOLDER") {
                for (logoLine in logoLines) {
                    bootLogs.add(logoLine)
                    delay(30L)
                }
            } else {
                bootLogs.add(line)
                delay(if (line.startsWith("-")) 30L else 120L)
            }
        }
        delay(800L)
        onBootComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        bootLogs.forEach { log ->
            val isLogoLine = logoLines.contains(log)
            Text(
                text = log,
                color = terminalColor,
                fontSize = if (isLogoLine) 8.sp else 12.sp,
                fontFamily = TerminalFontFamily,
                lineHeight = if (isLogoLine) 8.sp else 18.sp,
                softWrap = false,
                letterSpacing = if (isLogoLine) 0.sp else 0.4.sp
            )
        }
    }
}
