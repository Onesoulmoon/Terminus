package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class AsciiParticle(
    val id: Int,
    val char: String,
    val targetX: Float,
    val targetY: Float
)

@Composable
fun AsciiBlockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    terminalColor: Color = Color(0xFF00FF66)
) {
    val coroutineScope = rememberCoroutineScope()
    val animProgress = remember { Animatable(0f) }
    var particles by remember { mutableStateOf<List<AsciiParticle>>(emptyList()) }

    fun triggerBurst() {
        val glyphs = listOf("█", "▓", "▒", "░", "#", "+", "*", "░")
        particles = List(12) { index ->
            val angle = Random.nextFloat() * 2 * Math.PI
            val distance = Random.nextFloat() * 60f + 20f
            AsciiParticle(
                id = index,
                char = glyphs.random(),
                targetX = (Math.cos(angle) * distance).toFloat(),
                targetY = (Math.sin(angle) * distance).toFloat()
            )
        }
        coroutineScope.launch {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            )
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            triggerBurst()
            onClick()
        }
    ) {
        // Center Terminal Control Label
        Text(
            text = text,
            color = terminalColor,
            fontSize = 18.sp,
            fontFamily = TerminalFontFamily
        )

        // Dissolving Particle Matrix Layer
        if (animProgress.value < 1f && particles.isNotEmpty()) {
            particles.forEach { particle ->
                val currentX = particle.targetX * animProgress.value
                val currentY = particle.targetY * animProgress.value
                val alpha = (1f - animProgress.value).coerceIn(0f, 1f)

                Text(
                    text = particle.char,
                    color = terminalColor.copy(alpha = alpha),
                    fontSize = 10.sp,
                    fontFamily = TerminalFontFamily,
                    modifier = Modifier.offset {
                        IntOffset(currentX.dp.roundToPx(), currentY.dp.roundToPx())
                    }
                )
            }
        }
    }
}
