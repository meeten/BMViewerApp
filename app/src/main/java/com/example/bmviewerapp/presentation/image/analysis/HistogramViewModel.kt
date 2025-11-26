package com.example.bmviewerapp.presentation.image.analysis

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel

class HistogramViewModel : ViewModel() {
    fun calculateHistogram(bitmap: Bitmap?): List<Int> {
        if (bitmap == null) return List(256) { 0 }

        val histogram = IntArray(256) { 0 }
        val pixels = IntArray(bitmap.width * bitmap.height)

        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            // Вычисляем яркость по формуле luminance
            val luminance = (Color.red(pixel) * 0.299f +
                    Color.green(pixel) * 0.587f +
                    Color.blue(pixel) * 0.114f).toInt()
            histogram[luminance.coerceIn(0, 255)]++
        }

        return histogram.toList()
    }
}