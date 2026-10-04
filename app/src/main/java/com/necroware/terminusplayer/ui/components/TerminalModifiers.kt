package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

/**
 * Cyberpunk interaction language using InteractionSource for list reliability.
 */

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.terminalInteraction(
    glowColor: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 0f,
        animationSpec = tween(100),
        label = "glow"
    )

    this
        .scale(scale)
        .drawWithContent {
            drawContent()
            if (glowAlpha > 0f) {
                drawRect(
                    color = glowColor.copy(alpha = glowAlpha * 0.15f),
                    size = size
                )
            }
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        )
}

/**
 * Scroll-safe version of terminalInteraction.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.safeTerminalInteraction(
    listState: LazyListState,
    glowColor: Color = Color.White,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 0f,
        animationSpec = tween(100),
        label = "glow"
    )

    this
        .scale(scale)
        .drawWithContent {
            drawContent()
            if (glowAlpha > 0f) {
                drawRect(
                    color = glowColor.copy(alpha = glowAlpha * 0.15f),
                    size = size
                )
            }
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                if (!listState.isScrollInProgress) {
                    onClick()
                }
            },
            onLongClick = {
                if (!listState.isScrollInProgress) {
                    onLongClick?.invoke()
                }
            }
        )
}

fun Modifier.terminalClick(
    glowColor: Color = Color.White,
    onClick: () -> Unit
) = terminalInteraction(glowColor = glowColor, onClick = onClick)
