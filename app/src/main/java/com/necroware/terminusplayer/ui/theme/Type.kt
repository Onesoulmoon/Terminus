package com.necroware.terminusplayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.R

val TerminalFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

val TerminusTypography = Typography(
    displayLarge = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = TerminalFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 1.sp)
)
