package com.example.bmviewerapp.presentation.image.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FilterStateViewModel : ViewModel() {

    var filterParams by mutableStateOf(ImageFilterParams())
        private set

    fun updateBrightness(brightness: Float) {
        filterParams = filterParams.copy(brightness = brightness)
    }

    fun updateContrast(contrast: Float) {
        filterParams = filterParams.copy(contrast = contrast)
    }

    fun updateOffsetTop(offsetTop: Float) {
        filterParams = filterParams.copy(histogramOffsetTop = offsetTop)
    }

    fun updateOffsetBottom(offsetBottom: Float) {
        filterParams = filterParams.copy(histogramOffsetBottom = offsetBottom)
    }

    fun resetAll() {
        filterParams = ImageFilterParams()
    }
}