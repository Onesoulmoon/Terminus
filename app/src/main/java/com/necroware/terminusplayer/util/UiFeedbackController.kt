package com.necroware.terminusplayer.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiFeedbackController @Inject constructor() {
    private val _queuedFeedbackVisible = MutableStateFlow(false)
    val queuedFeedbackVisible: StateFlow<Boolean> = _queuedFeedbackVisible

    fun showQueued() {
        _queuedFeedbackVisible.value = true
    }

    fun dismissQueued() {
        _queuedFeedbackVisible.value = false
    }
}
