package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.lyrics.LyricLine
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

@Composable
fun CrtScanlineOverlay(
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00FFC2).copy(alpha = 0.04f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokePx = 1.dp.toPx()
        val spacingPx = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokePx
            )
            y += spacingPx
        }
    }
}

@Composable
fun TerminalCmdWordByWordLine(
    line: LyricLine,
    currentPositionMs: Long,
    isActive: Boolean,
    isPast: Boolean,
    fontSize: TextUnit = 13.sp,
    activeColor: Color = Color(0xFF00FFC2),
    pastColor: Color = Color(0xFF336644),
    upcomingColor: Color = Color.Gray,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "cursorAlpha"
    )

    if (!isActive) {
        val textColor = if (isPast) pastColor else upcomingColor
        Text(
            text = line.text,
            color = textColor,
            fontFamily = TerminalFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            softWrap = true,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        val elapsedInLineMs = (currentPositionMs - line.timestampMs).coerceAtLeast(0L)
        val textLength = line.text.length
        val typedCharCount = if (textLength > 0) {
            (elapsedInLineMs / 35L).toInt().coerceIn(1, textLength)
        } else 0

        val revealedText = line.text.take(typedCharCount)

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = revealedText,
                color = activeColor,
                fontFamily = TerminalFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                softWrap = true,
                modifier = Modifier.weight(1f, fill = false)
            )
            Text(
                text = "_",
                color = activeColor.copy(alpha = cursorAlpha),
                fontFamily = TerminalFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TerminalLyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    isLoading: Boolean,
    sourceTag: String? = null,
    errorMessage: String? = null,
    fontSizeSp: TextUnit = 14.sp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val palette = LocalTerminalPalette.current

    val activeIndex = remember(currentPositionMs, lyrics) {
        val index = lyrics.indexOfLast { it.timestampMs <= currentPositionMs }
        if (index >= 0) index else 0
    }

    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty()) {
            listState.animateScrollToItem(
                index = (activeIndex - 2).coerceAtLeast(0)
            )
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFF050B08))
            .border(2.dp, palette.mutedAccent)
            .padding(8.dp)
    ) {
        CrtScanlineOverlay(lineColor = palette.primaryAccent.copy(alpha = 0.04f))

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "[ FETCHING_LYRICS_STREAM... ]",
                    color = palette.primaryAccent,
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp
                )
            }
        } else if (lyrics.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NO_LYRIC_DATA // 0x404",
                    color = palette.secondaryAccent,
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = errorMessage ?: "[ERR_404: TRACK_NOT_FOUND_IN_REMOTE_INDEX]",
                    color = palette.secondaryAccent.copy(alpha = 0.7f),
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isActive) palette.mutedAccent.copy(alpha = 0.2f) else Color.Transparent)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isActive) ">> " else "┆  ",
                            color = if (isActive) palette.secondaryAccent else Color.DarkGray,
                            fontFamily = TerminalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        TerminalCmdWordByWordLine(
                            line = line,
                            currentPositionMs = currentPositionMs,
                            isActive = isActive,
                            isPast = isPast,
                            fontSize = fontSizeSp,
                            activeColor = palette.primaryAccent,
                            pastColor = palette.mutedAccent,
                            upcomingColor = Color.Gray
                        )
                    }
                }
            }
        }

        Text(
            text = sourceTag ?: "LYRIC_SYNC // JETBRAINS_MONO",
            color = palette.primaryAccent.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
