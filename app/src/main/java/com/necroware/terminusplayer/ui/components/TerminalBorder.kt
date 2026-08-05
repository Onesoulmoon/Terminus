package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Thin rectangular border in the accent color, mimicking a terminal
 * box-drawing frame without literal ASCII box-drawing glyphs (which
 * render inconsistently across fonts). Use around cards/sections.
 */
@Composable
fun TerminalBorder(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
    ) {
        content()
    }
}
