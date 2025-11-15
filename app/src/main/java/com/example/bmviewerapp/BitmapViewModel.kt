package com.example.bmviewerapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel

class BitmapViewModel : ViewModel() {
    fun parseBmpFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.d("URI_TO_BMP", e.message.toString())
            null
        }
    }

    fun invertBitmapColors(originalBitmap: Bitmap?): Bitmap? {
        if (originalBitmap == null) return null

        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(originalBitmap.width * originalBitmap.height)

        mutableBitmap.getPixels(
            pixels,
            0,
            originalBitmap.width,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height
        )

        for (i in 0 until pixels.size) {
            val alpha = Color.alpha(pixels[i])
            val red = Color.red(pixels[i])
            val green = Color.green(pixels[i])
            val blue = Color.blue(pixels[i])

            val convertRed = 255 - red
            val convertGreen = 255 - green
            val convertBlue = 255 - blue

            pixels[i] = Color.argb(alpha, convertRed, convertGreen, convertBlue)
        }

        mutableBitmap.setPixels(
            pixels,
            0,
            originalBitmap.width,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height
        )

        return mutableBitmap
    }
}