package com.necroware.terminusplayer.ui.components

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import kotlin.random.Random

// AGSL CRT Distortion Shader (Android 13 / API 33+)
private const val CRT_AGSL_SHADER = """
    uniform shader composable;
    uniform float2 size;
    uniform float time;

    half4 main(float2 fragCoord) {
        // Normalize UV coordinates to [-1, 1]
        float2 uv = (fragCoord / size) * 2.0 - 1.0;

        // Pincushion / Curved Glass Barrel Distortion ("Bombed Screen")
        float distortion = 0.18;
        uv += uv * (uv.x * uv.x + uv.y * uv.y) * distortion;

        // Map back to normalized [0, 1] UV space
        uv = (uv + 1.0) * 0.5;

        // Black out pixels outside the curved glass boundary
        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
            return half4(0.0, 0.0, 0.0, 1.0);
        }

        // Chromatic Aberration Shift (RGB Color Separation)
        float redShift = 0.004;
        float blueShift = -0.004;
        
        half4 color;
        color.r = composable.eval(float2(uv.x + redShift, uv.y) * size).r;
        color.g = composable.eval(uv * size).g;
        color.b = composable.eval(float2(uv.x + blueShift, uv.y) * size).b;
        color.a = 1.0;

        // CRT Scanline Overlay
        float scanline = sin(uv.y * size.y * 1.2) * 0.12;
        color.rgb -= scanline;

        // Corner Vignette Edge Darkening
        float vignette = uv.x * uv.y * (1.0 - uv.x) * (1.0 - uv.y);
        vignette = clamp(pow(16.0 * vignette, 0.35), 0.0, 1.0);
        color.rgb *= vignette;

        return color;
    }
"""

@Composable
fun CrtCathodiqueAlbumArt(
    bitmap: Bitmap?,
    trackTitle: String = "SULLY_SHIFT_01",
    modifier: Modifier = Modifier
) {
    // Infinite animation cycle for static noise & CRT OSD pulse
    val infiniteTransition = rememberInfiniteTransition(label = "crtAnim")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "time"
    )

    // Inner Curved Glass CRT Tube
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp) // Restore surround
            .clip(RoundedCornerShape(18)) // Curving the display edges
            .background(Color.Black)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = remember { RuntimeShader(CRT_AGSL_SHADER) }
                    Modifier.graphicsLayer {
                        shader.setFloatUniform("size", size.width, size.height)
                        shader.setFloatUniform("time", time)
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else Modifier
            )
    ) {
        // 1. Base Album Art / Placeholder Signal
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "CRT Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF001100)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO SIGNAL // 0x04",
                    color = Color(0xFF00FF66),
                    fontFamily = TerminalFontFamily,
                    fontSize = 14.sp
                )
            }
        }

        // 2. Analog Static Grain Generator
        Canvas(modifier = Modifier.fillMaxSize()) {
            val noiseCount = 120
            for (i in 0..noiseCount) {
                val rx = Random.nextFloat() * size.width
                val ry = Random.nextFloat() * size.height
                val alpha = Random.nextFloat() * 0.15f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 1.5f,
                    center = Offset(rx, ry)
                )
            }
        }

        // 3. Fallback Scanline Layer for Android < 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineSpacing = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f
                    )
                    y += lineSpacing
                }
            }
        }

        // 4. Retro ViewSonic OSD Overlay ("PINCUSHION 88" Style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 16.dp)
                .background(Color(0xFF0000AA).copy(alpha = 0.85f)) // Retro Blue OSD Box
                .border(1.dp, Color(0xFF00FFFF))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = "PINCUSHION: 88  [ ░░░░░█ ]",
                    color = Color.White,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
                Text(
                    text = "TRACK // $trackTitle",
                    color = Color(0xFF00FF66),
                    fontFamily = TerminalFontFamily,
                    fontSize = 9.sp
                )
            }
        }

        // 5. Curved Glass Light Reflection Glare
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.7f, size.height * 0.7f)
                )
            )
        }
    }
}
