package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object Glyph {
    const val PREVIOUS = "|<<"
    const val PLAY = ">"
    const val PAUSE = "||"
    const val NEXT = ">>|"
    const val SHUFFLE = "SHUF"
    const val REPEAT = "RPT"
    const val REPEAT_ONE = "RPT1"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AsciiSkipNextButton(
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    width: Dp = 62.dp,
    height: Dp = 62.dp
) {
    var isTriggered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val offsetX by animateFloatAsState(
        targetValue = if (isTriggered) 10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { isTriggered = false },
        label = "SkipNextNudge"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "press"
    )

    val accent = MaterialTheme.colorScheme.primary
    val displayText = if (isTriggered) "> >|" else ">>|"

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(1.dp, accent.copy(alpha = 0.35f))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    isTriggered = true
                    onSkipNext()
                },
                onLongClick = onLongPress?.let { longClick ->
                    {
                        isTriggered = true
                        longClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = accent,
            fontFamily = TerminalFontFamily,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { translationX = offsetX },
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AsciiSkipPreviousButton(
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    width: Dp = 62.dp,
    height: Dp = 62.dp
) {
    var isTriggered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetX by animateFloatAsState(
        targetValue = if (isTriggered) -10f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { isTriggered = false },
        label = "SkipPrevNudge"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "press"
    )

    val accent = MaterialTheme.colorScheme.primary
    val displayText = if (isTriggered) "|< <" else "|<<"

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(1.dp, accent.copy(alpha = 0.35f))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    isTriggered = true
                    onSkipPrevious()
                },
                onLongClick = onLongPress?.let { longClick ->
                    {
                        isTriggered = true
                        longClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = accent,
            fontFamily = TerminalFontFamily,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { translationX = offsetX },
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@Composable
fun AsciiPlayPauseButton(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 82.dp,
    height: Dp = 82.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PlayPauseScale"
    )

    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(accent)
            .clickable(interactionSource = interactionSource, indication = null) {
                onTogglePlay()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            contentAlignment = Alignment.Center,
            label = "playPauseFade"
        ) { playing ->
            Text(
                text = if (playing) "||" else ">",
                color = MaterialTheme.colorScheme.onPrimary,
                fontFamily = TerminalFontFamily,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

@Composable
fun ExtendedTransportControlsRow(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    repeatMode: Int,
    onTogglePlay: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPressPrevious: (() -> Unit)? = null,
    onLongPressNext: (() -> Unit)? = null
) {
    val isRepeatOn = repeatMode != Player.REPEAT_MODE_OFF
    val repeatText = if (repeatMode == Player.REPEAT_MODE_ONE) TerminusSymbols.REPEAT_ONE else TerminusSymbols.REPEAT

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Shuffle (↹)
        SquareButton(
            text = TerminusSymbols.SHUFFLE,
            onClick = onToggleShuffle,
            isActive = isShuffleOn,
            width = 46.dp,
            height = 46.dp,
            fontSize = 18,
            useBlockAnimation = true
        )

        // 2. Skip Previous (|<<)
        AsciiSkipPreviousButton(
            onSkipPrevious = onSkipPrevious,
            onLongPress = onLongPressPrevious
        )

        // 3. Play / Pause Fade (> / ||)
        AsciiPlayPauseButton(
            isPlaying = isPlaying,
            onTogglePlay = onTogglePlay
        )

        // 4. Skip Next (>>|)
        AsciiSkipNextButton(
            onSkipNext = onSkipNext,
            onLongPress = onLongPressNext
        )

        // 5. Repeat (↻ / ↻1) with spinning animation
        SquareButton(
            text = repeatText,
            onClick = onToggleRepeat,
            isActive = isRepeatOn,
            width = 46.dp,
            height = 46.dp,
            fontSize = if (repeatMode == Player.REPEAT_MODE_ONE) 14 else 18,
            useBlockAnimation = true,
            enableSpinOnClick = true
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SquareButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isInverse: Boolean = false,
    isActive: Boolean = false,
    width: Dp = 56.dp,
    height: Dp = 56.dp,
    fontSize: Int = 14,
    useBlockAnimation: Boolean = false,
    enableSpinOnClick: Boolean = false
) {
    val accent = MaterialTheme.colorScheme.primary
    val onSurface = if (isInverse) MaterialTheme.colorScheme.onPrimary else accent
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    
    var blockState by remember { mutableIntStateOf(3) } // 0: ░, 1: ▒, 2: ▓, 3: Full
    var spinAngleTarget by remember { mutableFloatStateOf(0f) }
    val spinAngle by animateFloatAsState(
        targetValue = spinAngleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "spin"
    )
    
    fun triggerAnimation() {
        if (!useBlockAnimation) return
        scope.launch {
            blockState = 0
            delay(40)
            blockState = 1
            delay(40)
            blockState = 2
            delay(40)
            blockState = 3
        }
    }

    LaunchedEffect(text) {
        triggerAnimation()
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "press"
    )

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = spinAngle
            }
            .background(if (isInverse) accent else if (isActive) accent.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (isInverse) accent else if (isActive) accent else accent.copy(alpha = 0.35f))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (enableSpinOnClick) spinAngleTarget += 360f
                    triggerAnimation()
                    onClick()
                },
                onLongClick = onLongClick?.let { longClick ->
                    {
                        if (enableSpinOnClick) spinAngleTarget += 360f
                        triggerAnimation()
                        longClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (useBlockAnimation && blockState < 3) {
            val blockChar = when (blockState) {
                0 -> "░"
                1 -> "▒"
                2 -> "▓"
                else -> ""
            }
            // Centered 5x5 grid for tactical feel without overflow
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    repeat(5) {
                        Text(
                            text = blockChar.repeat(5),
                            color = onSurface,
                            fontSize = (fontSize - 2).sp,
                            lineHeight = (fontSize - 2).sp,
                            fontFamily = TerminalFontFamily
                        )
                    }
                }
            }
        } else {
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    fadeIn(tween(250)) togetherWith fadeOut(tween(250))
                },
                contentAlignment = Alignment.Center,
                label = "glyph"
            ) { targetText ->
                Text(
                    text = targetText,
                    color = onSurface,
                    fontSize = fontSize.sp,
                    lineHeight = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Composable
fun FullTransportControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPressPrevious: (() -> Unit)? = null,
    onLongPressNext: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquareButton(
            text = Glyph.SHUFFLE,
            onClick = onToggleShuffle,
            isActive = shuffleEnabled,
            width = 46.dp,
            height = 46.dp,
            fontSize = 9,
            useBlockAnimation = true
        )
        SquareButton(
            text = Glyph.PREVIOUS,
            onClick = onSkipPrevious,
            onLongClick = onLongPressPrevious,
            width = 62.dp,
            height = 62.dp,
            fontSize = 16,
            useBlockAnimation = true
        )
        SquareButton(
            text = if (isPlaying) Glyph.PAUSE else Glyph.PLAY,
            onClick = onTogglePlayPause,
            isInverse = true,
            width = 82.dp,
            height = 82.dp,
            fontSize = 24
        )
        SquareButton(
            text = Glyph.NEXT,
            onClick = onSkipNext,
            onLongClick = onLongPressNext,
            width = 62.dp,
            height = 62.dp,
            fontSize = 16,
            useBlockAnimation = true
        )
        SquareButton(
            text = if (repeatMode == Player.REPEAT_MODE_ONE) Glyph.REPEAT_ONE else Glyph.REPEAT,
            onClick = onCycleRepeat,
            isActive = repeatMode != Player.REPEAT_MODE_OFF,
            width = 46.dp,
            height = 46.dp,
            fontSize = 9,
            useBlockAnimation = true
        )
    }
}

@Composable
fun CompactTransportControls(
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPressPrevious: (() -> Unit)? = null,
    onLongPressNext: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquareButton(text = Glyph.PREVIOUS, onClick = onSkipPrevious, onLongClick = onLongPressPrevious, width = 32.dp, height = 32.dp, fontSize = 10, useBlockAnimation = true)
        SquareButton(text = if (isPlaying) Glyph.PAUSE else Glyph.PLAY, onClick = onTogglePlayPause, isInverse = true, width = 38.dp, height = 38.dp, fontSize = 14)
        SquareButton(text = Glyph.NEXT, onClick = onSkipNext, onLongClick = onLongPressNext, width = 32.dp, height = 32.dp, fontSize = 10, useBlockAnimation = true)
    }
}
