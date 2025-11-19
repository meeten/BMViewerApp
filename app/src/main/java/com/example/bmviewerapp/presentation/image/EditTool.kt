package com.example.bmviewerapp.presentation.image

import com.example.bmviewerapp.R

enum class EditTool(val iconRes: Int, val title: String, val isMain: Boolean = true) {

    HISTOGRAM(iconRes = R.drawable.histogram, title = "Histogram"),

    BRIGHTNESS(iconRes = R.drawable.brightness, title = "Brightness"),

    INVERT_COLORS(iconRes = R.drawable.invert_colors, title = "Invert"),

    EMBOSS(iconRes = R.drawable.emboss, title = "Emboss"),

    BLUR(iconRes = R.drawable.blur, title = "Blur", false),

    CONTOUR(iconRes = R.drawable.contour, title = "Contour", false),

    SHARP(iconRes = R.drawable.sharp, title = "Sharp", false)
}