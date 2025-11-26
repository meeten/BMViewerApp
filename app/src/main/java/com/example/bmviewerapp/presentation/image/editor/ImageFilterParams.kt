package com.example.bmviewerapp.presentation.image.editor

data class ImageFilterParams(
    val histogramOffsetTop: Float = 0.0f,
    val histogramOffsetBottom: Float = 0.0f,
    val brightness: Float = 0.0f,
    val contrast: Float = 1.25f
)