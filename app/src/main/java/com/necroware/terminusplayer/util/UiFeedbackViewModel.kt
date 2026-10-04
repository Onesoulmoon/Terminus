package com.necroware.terminusplayer.util

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UiFeedbackViewModel @Inject constructor(
    val controller: UiFeedbackController
) : ViewModel()
