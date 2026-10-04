package com.necroware.terminusplayer.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Prevents accidental song triggers while scrolling through lists.
 */
fun Modifier.safeItemClick(
    listState: LazyListState,
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        // Discard click if the list is scrolling or fling decelerating
        if (!listState.isScrollInProgress) {
            onClick()
        }
    }
}

/**
 * Overload for Grid state.
 */
fun Modifier.safeGridItemClick(
    gridState: LazyGridState,
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        if (!gridState.isScrollInProgress) {
            onClick()
        }
    }
}
