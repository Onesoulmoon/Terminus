package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.LocalTerminusAccent
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AsciiMusicHeader(
    modifier: Modifier = Modifier,
    cycleDelayMs: Long = 250L,
    fontSize: TextUnit = 20.sp,
    color: Color = LocalTerminalPalette.current.tertiaryAccent,
    showBrackets: Boolean = true
) {
    val musicSymbols = remember { listOf("𝄞", "𝄢", "𝄩", "♩") }
    var symbolIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(cycleDelayMs)
            symbolIndex = (symbolIndex + 1) % musicSymbols.size
        }
    }

    val text = if (showBrackets) "[ ${musicSymbols[symbolIndex]} ]" else musicSymbols[symbolIndex]

    Text(
        text = text,
        color = color,
        fontFamily = TerminalFontFamily,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun TerminalTypewriterHeader(
    text: String,
    modifier: Modifier = Modifier,
    typingDelayMs: Long = 40L,
    cursorBlinkMs: Long = 500L,
    fontSize: TextUnit = 22.sp,
    color: Color = LocalTerminalPalette.current.primaryAccent,
    cursorColor: Color = LocalTerminalPalette.current.highlightAccent
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var isCursorVisible by remember { mutableStateOf(true) }

    // 1. Typewriter Effect Logic (Triggers when 'text' changes)
    LaunchedEffect(text) {
        displayedText = ""
        text.forEach { char ->
            displayedText += char
            delay(typingDelayMs)
        }
    }

    // 2. Continuous Blinking Cursor Loop
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(cursorBlinkMs)
            isCursorVisible = !isCursorVisible
        }
    }

    val cursorChar = if (isCursorVisible) "_" else " "

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = displayedText,
            color = color,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = cursorChar,
            color = cursorColor,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TerminusTopHeader(
    currentTabName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dynamic Typewriter Title with Blinking Cursor
        TerminalTypewriterHeader(
            text = "> $currentTabName",
            fontSize = 18.sp
        )

        // Animated Music Symbol Ticker
        AsciiMusicHeader()
    }
}
