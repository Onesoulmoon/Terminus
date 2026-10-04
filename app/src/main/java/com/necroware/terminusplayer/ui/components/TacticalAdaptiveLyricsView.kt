package com.necroware.terminusplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.lyrics.LyricLine
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

@Composable
fun TacticalAdaptiveLyricsView(
    lyrics: List<LyricLine>,
    currentPositionMs: Long,
    albumArtBitmap: Bitmap?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    sourceTag: String? = null,
    errorMessage: String? = null,
    fontSizeSp: TextUnit = 14.sp
) {
    val listState = rememberLazyListState()
    val palette = LocalTerminalPalette.current

    val colorPool = remember(palette) {
        listOf(
            palette.primaryAccent,
            palette.secondaryAccent,
            palette.tertiaryAccent,
            palette.highlightAccent
        )
    }

    val activeIndex = remember(currentPositionMs, lyrics) {
        val index = lyrics.indexOfLast { it.timestampMs <= currentPositionMs }
        if (index >= 0) index else 0
    }

    LaunchedEffect(lyrics) {
        if (lyrics.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty() && activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF090A0C))
            .border(1.dp, palette.mutedAccent.copy(alpha = 0.4f))
            .padding(12.dp)
    ) {
        CrtScanlineOverlay(lineColor = palette.primaryAccent.copy(alpha = 0.04f))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "[ EXTRACTING_LYRIC_STREAM... ]",
                    color = palette.primaryAccent,
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp
                )
            }
        } else if (lyrics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                    val baseColor = colorPool[index % colorPool.size]
                    val animatedColor by animateColorAsState(
                        targetValue = if (isActive) baseColor else Color.Gray,
                        animationSpec = tween(durationMillis = 300),
                        label = "colorAnim"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .alpha(if (isActive) 1.0f else 0.40f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TerminalCmdWordByWordLine(
                            line = line,
                            currentPositionMs = currentPositionMs,
                            isActive = isActive,
                            isPast = isPast,
                            fontSize = fontSizeSp,
                            activeColor = animatedColor,
                            pastColor = Color.DarkGray,
                            upcomingColor = Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Text(
            text = sourceTag ?: "THEME_PRESET // ADAPTIVE",
            color = palette.primaryAccent.copy(alpha = 0.5f),
            fontFamily = TerminalFontFamily,
            fontSize = 8.sp,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
