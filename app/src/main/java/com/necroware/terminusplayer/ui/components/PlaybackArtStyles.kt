package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.data.prefs.PlaybackArtStyle

@Composable
fun PlaybackArt(
    style: PlaybackArtStyle,
    title: String,
    artist: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reels")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3000 else 0, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            PlaybackArtStyle.STANDARD -> {
                // Should not be called for STANDARD, handled in NowPlayingScreen
            }
            PlaybackArtStyle.CASSETTE -> CassettePlayer(title, artist, rotation)
            PlaybackArtStyle.REEL_TO_REEL -> ReelToReel(rotation)
            PlaybackArtStyle.VINYL -> VinylRecord(rotation)
            PlaybackArtStyle.VHS -> VhsTape(title, artist, rotation)
        }
    }
}

@Composable
private fun CassettePlayer(title: String, artist: String, rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Main Body
        drawRoundRect(
            color = Color(0xFF2C2C2C),
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
        )
        
        // Label area
        drawRect(
            color = Color.White,
            topLeft = Offset(w * 0.05f, h * 0.1f),
            size = Size(w * 0.9f, h * 0.5f)
        )
        
        // Tape window
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(w * 0.25f, h * 0.65f),
            size = Size(w * 0.5f, h * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Left Reel
        rotate(rotation, pivot = Offset(w * 0.35f, h * 0.75f)) {
            drawCircle(Color.Gray, radius = 20f, center = Offset(w * 0.35f, h * 0.75f))
            drawLine(Color.White, Offset(w * 0.35f, h * 0.72f), Offset(w * 0.35f, h * 0.78f), strokeWidth = 4f)
        }
        
        // Right Reel
        rotate(rotation, pivot = Offset(w * 0.65f, h * 0.75f)) {
            drawCircle(Color.Gray, radius = 20f, center = Offset(w * 0.65f, h * 0.75f))
            drawLine(Color.White, Offset(w * 0.65f, h * 0.72f), Offset(w * 0.65f, h * 0.78f), strokeWidth = 4f)
        }
    }
    
    // Song Info on label
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.4f)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            color = Color.Blue,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artist,
            color = Color.DarkGray,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReelToReel(rotation: Float) {
    val primary = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Base plate
        drawRect(Color(0xFFE0E0E0), size = size)
        
        // Left Large Reel
        rotate(rotation, pivot = Offset(w * 0.28f, h * 0.4f)) {
            drawCircle(Color.White, radius = w * 0.25f, center = Offset(w * 0.28f, h * 0.4f))
            drawCircle(Color.Black, radius = w * 0.25f, center = Offset(w * 0.28f, h * 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            for (i in 0..2) {
                rotate(i * 120f, pivot = Offset(w * 0.28f, h * 0.4f)) {
                    drawRect(Color.Black, topLeft = Offset(w * 0.27f, h * 0.2f), size = Size(w * 0.02f, h * 0.2f))
                }
            }
        }

        // Right Large Reel
        rotate(rotation, pivot = Offset(w * 0.72f, h * 0.4f)) {
            drawCircle(Color.White, radius = w * 0.25f, center = Offset(w * 0.72f, h * 0.4f))
            drawCircle(Color.Black, radius = w * 0.25f, center = Offset(w * 0.72f, h * 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            for (i in 0..2) {
                rotate(i * 120f, pivot = Offset(w * 0.72f, h * 0.4f)) {
                    drawRect(Color.Black, topLeft = Offset(w * 0.71f, h * 0.2f), size = Size(w * 0.02f, h * 0.2f))
                }
            }
        }
        
        // Bottom controls area
        drawRect(Color.Gray, topLeft = Offset(0f, h * 0.75f), size = Size(w, h * 0.25f))
        drawCircle(primary, radius = 15f, center = Offset(w * 0.2f, h * 0.85f))
        drawCircle(primary, radius = 15f, center = Offset(w * 0.4f, h * 0.85f))
        drawCircle(Color.Red, radius = 15f, center = Offset(w * 0.6f, h * 0.85f))
    }
}

@Composable
private fun VinylRecord(rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        
        // The Vinyl
        drawCircle(Color.Black, radius = radius, center = center)
        
        // Grooves
        for (i in 1..10) {
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius * (i / 10f),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
            )
        }
        
        // Label
        rotate(rotation) {
            drawCircle(Color(0xFFD32F2F), radius = radius * 0.35f, center = center)
            drawCircle(Color.White, radius = 10f, center = center)
            drawLine(Color.White, Offset(center.x - 20, center.y - 30), Offset(center.x + 20, center.y - 30), strokeWidth = 2f)
        }
        
        // Tonearm
        val armPath = Path().apply {
            moveTo(size.width * 0.9f, size.height * 0.1f)
            lineTo(size.width * 0.85f, size.height * 0.6f)
            lineTo(size.width * 0.6f, size.height * 0.55f)
        }
        drawPath(armPath, Color.LightGray, style = androidx.compose.ui.graphics.drawscope.Stroke(15f))
    }
}

@Composable
private fun VhsTape(title: String, artist: String, rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // VHS Body
        drawRect(Color(0xFF1A1A1A), size = size)
        
        // The flap at the top
        drawRect(Color.Black, size = Size(w, h * 0.15f))
        
        // Window
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.25f),
            size = Size(w * 0.8f, h * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
        )
        
        // Reels visible through window
        rotate(rotation, pivot = Offset(w * 0.3f, h * 0.42f)) {
            drawCircle(Color.White.copy(alpha = 0.9f), radius = w * 0.12f, center = Offset(w * 0.3f, h * 0.42f))
            drawCircle(Color.Black, radius = 15f, center = Offset(w * 0.3f, h * 0.42f))
        }
        rotate(rotation, pivot = Offset(w * 0.7f, h * 0.42f)) {
            drawCircle(Color.White.copy(alpha = 0.9f), radius = w * 0.12f, center = Offset(w * 0.7f, h * 0.42f))
            drawCircle(Color.Black, radius = 15f, center = Offset(w * 0.7f, h * 0.42f))
        }
        
        // Spine/Label area
        drawRect(Color.White, topLeft = Offset(w * 0.05f, h * 0.7f), size = Size(w * 0.9f, h * 0.2f))
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.padding(bottom = 35.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.ifBlank { "Untitled" },
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                color = Color.DarkGray,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}
