package com.necroware.terminusplayer

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.necroware.terminusplayer.data.prefs.DynamicThemeMode
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.data.prefs.UserPreferences
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.playback.PlaybackController
import com.necroware.terminusplayer.ui.theme.ThemePreset
import com.necroware.terminusplayer.ui.theme.ThemePresets
import com.necroware.terminusplayer.ui.theme.themePresetById
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _dynamicTheme = MutableStateFlow<ThemePreset?>(null)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val currentTheme: StateFlow<ThemePreset> = combine(
        preferencesRepository.preferences,
        _dynamicTheme
    ) { prefs, dynamic ->
        val base = themePresetById(prefs.themeId)
        if (prefs.useDynamicTheme && dynamic != null) {
            dynamic
        } else {
            base
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePresets.first())

    init {
        playbackController.connect()
        observeArtworkForDynamicTheme()
    }

    fun playExternalUri(uri: Uri) {
        playbackController.playExternalUri(uri)
    }

    private fun observeArtworkForDynamicTheme() {
        viewModelScope.launch {
            combine(
                playbackController.state.map { it.artworkUri }.distinctUntilChanged(),
                userPreferences.map { it.dynamicThemeMode }.distinctUntilChanged(),
                userPreferences.map { it.useDynamicTheme }.distinctUntilChanged()
            ) { uri, mode, useDynamic ->
                Triple(uri, mode, useDynamic)
            }.collect { (uri, _, useDynamic) ->
                if (useDynamic && uri != null) {
                    updateDynamicThemeFromUri(uri)
                } else {
                    _dynamicTheme.value = null
                }
            }
        }
    }

    private suspend fun updateDynamicThemeFromUri(uriString: String) {
        val bitmap = withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                context.contentResolver.loadThumbnail(uri, android.util.Size(220, 220), null)
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap == null) {
            _dynamicTheme.value = null
            return
        }

        val palette = withContext(Dispatchers.Default) {
            runCatching { Palette.from(bitmap).generate() }.getOrNull()
        }
        if (palette == null) {
            _dynamicTheme.value = null
            return
        }

        fun rgbLuma(color: Int): Double = (
            0.2126 * android.graphics.Color.red(color) +
                0.7152 * android.graphics.Color.green(color) +
                0.0722 * android.graphics.Color.blue(color)
            ) / 255.0

        fun contrastRatio(foreground: Int, background: Int): Double {
            fun lum(c: Int): Double {
                fun channel(v: Int): Double {
                    val s = v / 255.0
                    return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
                }
                return 0.2126 * channel(android.graphics.Color.red(c)) +
                    0.7152 * channel(android.graphics.Color.green(c)) +
                    0.0722 * channel(android.graphics.Color.blue(c))
            }
            val a = lum(foreground) + 0.05
            val b = lum(background) + 0.05
            return maxOf(a, b) / minOf(a, b)
        }

        fun blend(a: Color, b: Color, t: Float): Color = Color(
            a.red + (b.red - a.red) * t.coerceIn(0f, 1f),
            a.green + (b.green - a.green) * t.coerceIn(0f, 1f),
            a.blue + (b.blue - a.blue) * t.coerceIn(0f, 1f),
            1f
        )

        fun argb(color: Color) = color.toArgb()

        // Never use pure black artwork as an accent. Prefer a real vibrant
        // color, then a light-vibrant color, then a neutral high-contrast
        // fallback. B/W album art therefore becomes a silver/white terminal
        // palette instead of black-on-black controls.
        val vibrant = palette.getVibrantColor(Color.Transparent.toArgb())
        val lightVibrant = palette.getLightVibrantColor(Color.Transparent.toArgb())
        val muted = palette.getMutedColor(Color.Transparent.toArgb())
        val lightMuted = palette.getLightMutedColor(Color.Transparent.toArgb())
        val dominant = palette.getDominantColor(Color.Black.toArgb())

        val rawAccent = when {
            vibrant != Color.Transparent.toArgb() -> Color(vibrant)
            lightVibrant != Color.Transparent.toArgb() -> Color(lightVibrant)
            muted != Color.Transparent.toArgb() -> Color(muted)
            lightMuted != Color.Transparent.toArgb() -> Color(lightMuted)
            else -> Color(0xFFE8E8E8)
        }

        val isLightMode = userPreferences.value.dynamicThemeMode == DynamicThemeMode.LIGHT

        val artColor = Color(dominant)
        val tintColor = if (rgbLuma(dominant) < 0.1) rawAccent else artColor

        val background = if (isLightMode) {
            blend(Color(0xFFF8F9FA), tintColor, 0.12f)
        } else {
            blend(Color(0xFF080808), tintColor, 0.25f)
        }

        val surface = if (isLightMode) {
            blend(background, Color.White, 0.60f)
        } else {
            blend(background, Color.White, 0.10f)
        }

        val elevated = if (isLightMode) {
            blend(surface, Color(0xFFE2E8F0), 0.50f)
        } else {
            blend(surface, rawAccent, 0.15f)
        }

        val border = if (isLightMode) {
            blend(rawAccent, Color(0xFF64748B), 0.50f)
        } else {
            blend(rawAccent, Color.White, 0.35f)
        }

        var accent = rawAccent
        if (isLightMode) {
            if (rgbLuma(argb(accent)) > 0.8) {
                accent = blend(accent, Color.Black, 0.3f)
            }
            var steps = 0
            while (contrastRatio(argb(accent), argb(background)) < 4.5 && steps < 12) {
                accent = blend(accent, Color.Black, 0.15f)
                steps++
            }
        } else {
            if (rgbLuma(argb(accent)) < 0.3) {
                accent = blend(accent, Color.White, 0.4f)
            }
            var steps = 0
            while (contrastRatio(argb(accent), argb(background)) < 4.5 && steps < 12) {
                accent = blend(accent, Color.White, 0.15f)
                steps++
            }
            if (contrastRatio(argb(accent), argb(background)) < 3.0) {
                accent = Color.White
            }
        }

        val textPrimary = if (isLightMode) Color(0xFF0F172A) else Color.White
        val textSecondary = if (isLightMode) Color(0xFF334155) else Color(0xFFE4E4E4)
        val textMuted = if (isLightMode) Color(0xFF64748B) else Color(0xFFACACAC)
        val onAccent = if (rgbLuma(argb(accent)) > 0.62) Color.Black else Color.White

        val safeElevated = if (contrastRatio(argb(textPrimary), argb(elevated)) < 3.5) {
            if (isLightMode) blend(elevated, Color.White, 0.35f) else blend(elevated, Color.Black, 0.35f)
        } else elevated

        _dynamicTheme.value = ThemePreset(
            id = ThemePresetId.DYNAMIC,
            label = if (isLightMode) "DYNAMIC (LIGHT)" else "DYNAMIC (DARK)",
            background = background,
            surface = surface,
            surfaceElevated = safeElevated,
            border = border,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            textMuted = textMuted,
            accent = accent,
            onAccent = onAccent
        )
    }

    override fun onCleared() {
        super.onCleared()
        playbackController.release()
    }
}
