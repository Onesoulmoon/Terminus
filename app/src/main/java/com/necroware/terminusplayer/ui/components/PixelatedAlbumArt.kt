package com.necroware.terminusplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

@Composable
fun PixelatedAlbumArt(
    bitmap: Bitmap?,
    pixelResolution: Int = 36, // Lower number = chunkier 8-bit pixels
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF00FF66)
) {
    // Generate low-res pixelated bitmap using nearest-neighbor sampling
    val pixelatedBitmap = remember(bitmap, pixelResolution) {
        bitmap?.let { src ->
            Bitmap.createScaledBitmap(src, pixelResolution, pixelResolution, false)
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.Black)
            .border(2.dp, borderColor),
        contentAlignment = Alignment.Center
    ) {
        if (pixelatedBitmap != null) {
            // Render downscaled bitmap using FilterQuality.None to preserve sharp pixel edges
            Image(
                bitmap = pixelatedBitmap.asImageBitmap(),
                contentDescription = "Pixelated Album Art",
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )

            // Optional Retro Pixel Grid Mesh Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stepX = size.width / pixelResolution
                val stepY = size.height / pixelResolution

                // Draw subtle dark pixel grid lines
                for (i in 0..pixelResolution) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.2f),
                        start = Offset(i * stepX, 0f),
                        end = Offset(i * stepX, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color.Black.copy(alpha = 0.2f),
                        start = Offset(0f, i * stepY),
                        end = Offset(size.width, i * stepY),
                        strokeWidth = 1f
                    )
                }
            }

            // Bottom-Right Pixel Specs Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${pixelResolution}x${pixelResolution}_PXL",
                    color = borderColor,
                    fontFamily = TerminalFontFamily,
                    fontSize = 8.sp
                )
            }
        } else {
            // Placeholder when no album art is loaded
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "[ █ █ █ ]",
                    color = borderColor,
                    fontFamily = TerminalFontFamily,
                    fontSize = 16.sp
                )
                Text(
                    text = "NO_ART_PXL",
                    color = Color.Gray,
                    fontFamily = TerminalFontFamily,
                    fontSize = 10.sp
                )
            }
        }
    }
}
