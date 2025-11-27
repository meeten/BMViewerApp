package com.example.bmviewerapp.presentation.image.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel

class BitmapViewModel() : ViewModel() {

    private var editHalf by mutableStateOf(false)

    fun setEditMode(editHalf: Boolean) {
        this.editHalf = editHalf
    }

    fun createPreviewBitmap(bitmap: Bitmap, scale: Float): Bitmap {
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        return bitmap.scale(width, height)
    }

    fun applyAllFilters(
        filterParams: ImageFilterParams,
        originalBitmap: Bitmap
    ): Bitmap {
        var result = originalBitmap

        val offsetTop = filterParams.histogramOffsetBottom
        val offsetBottom = filterParams.histogramOffsetTop
        val brightness = filterParams.brightness
        val contrast = filterParams.contrast

        if (offsetBottom != 0.0f || offsetTop != 0.0f) {
            result = histogramCorrectionBitmap(
                result,
                offsetBottom.toInt(),
                offsetTop.toInt()
            )
        }

        if (brightness != 0.0f) {
            result = brightnessBitmap(result, brightness)
        }

        if (contrast != 1.25f) {
            result = contrastBitmap(result, contrast)
        }

        return result
    }

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
            if (editHalf) {
                val x = i % originalBitmap.width
                if (x <= originalBitmap.width / 2) continue
            }

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

    fun brightnessBitmap(originalBitmap: Bitmap, brightness: Float): Bitmap {
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

        // brightness: -1.0f до +1.0f (-100% до +100%)
        val brightnessFactor = 1.0f + brightness

        for (i in 0 until pixels.size) {
            if (editHalf) {
                val x = i % originalBitmap.width
                if (x <= originalBitmap.width / 2) continue
            }

            val alpha = Color.alpha(pixels[i])
            var red = Color.red(pixels[i])
            var green = Color.green(pixels[i])
            var blue = Color.blue(pixels[i])

            red = (red * brightnessFactor).toInt().coerceIn(0, 255)
            green = (green * brightnessFactor).toInt().coerceIn(0, 255)
            blue = (blue * brightnessFactor).toInt().coerceIn(0, 255)

            pixels[i] = Color.argb(alpha, red, green, blue)
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

    fun contrastBitmap(originalBitmap: Bitmap, contrast: Float): Bitmap {
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

        // contrast: 0.5f до 2.0f (-50% до +100%)
        val contrastFactor = contrast * contrast

        for (i in 0 until pixels.size) {
            if (editHalf) {
                val x = i % originalBitmap.width
                if (x <= originalBitmap.width / 2) continue
            }

            val alpha = Color.alpha(pixels[i])
            var red = Color.red(pixels[i])
            var green = Color.green(pixels[i])
            var blue = Color.blue(pixels[i])

            red = (((red / 255.0 - 0.5) * contrastFactor + 0.5) * 255).toInt().coerceIn(0, 255)
            green = (((green / 255.0 - 0.5) * contrastFactor + 0.5) * 255).toInt().coerceIn(0, 255)
            blue = (((blue / 255.0 - 0.5) * contrastFactor + 0.5) * 255).toInt().coerceIn(0, 255)

            pixels[i] = Color.argb(alpha, red, green, blue)
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

    fun blurBitmap(originalBitmap: Bitmap?): Bitmap? {
        if (originalBitmap == null) return null

        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        // Фиксированный радиус 2 (матрица 3x3)
        val blurRadius = 2

        val startWithPixelIndex = if (editHalf) result.width / 2 else 0

        for (y in 0 until result.height) {
            for (x in startWithPixelIndex until result.width) {
                var totalA = 0
                var totalR = 0
                var totalG = 0
                var totalB = 0
                var count = 0

                // Матрица 3x3 вокруг текущего пикселя
                for (dy in -blurRadius..blurRadius) {
                    for (dx in -blurRadius..blurRadius) {
                        val nx = x + dx
                        val ny = y + dy

                        if (nx in 0 until result.width && ny in 0 until result.height) {
                            val pixel = pixels[ny * result.width + nx]
                            totalA += Color.alpha(pixel)
                            totalR += Color.red(pixel)
                            totalG += Color.green(pixel)
                            totalB += Color.blue(pixel)
                            count++
                        }
                    }
                }

                if (count > 0) {
                    pixels[y * result.width + x] = Color.argb(
                        totalA / count,
                        totalR / count,
                        totalG / count,
                        totalB / count
                    )
                }
            }
        }

        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun sharpenBitmap(originalBitmap: Bitmap?): Bitmap? {
        if (originalBitmap == null) return null

        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        // Матрица резкости (лапласиан)
        val sharpenMatrix = arrayOf(
            floatArrayOf(0f, -1f, 0f),
            floatArrayOf(-1f, 5f, -1f),
            floatArrayOf(0f, -1f, 0f)
        )

        val tempPixels = pixels.copyOf()

        val startWithPixelIndex = if (editHalf) result.width / 2 else 0

        for (y in 1 until result.height - 1) {
            for (x in startWithPixelIndex + 1 until result.width - 1) {
                var totalA = 0f
                var totalR = 0f
                var totalG = 0f
                var totalB = 0f

                // Применяем матрицу свертки 3x3
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nx = x + dx
                        val ny = y + dy
                        val pixel = tempPixels[ny * result.width + nx]
                        val weight = sharpenMatrix[dy + 1][dx + 1]

                        totalA += Color.alpha(pixel) * weight
                        totalR += Color.red(pixel) * weight
                        totalG += Color.green(pixel) * weight
                        totalB += Color.blue(pixel) * weight
                    }
                }

                // Ограничиваем значения и записываем результат
                pixels[y * result.width + x] = Color.argb(
                    totalA.coerceIn(0f, 255f).toInt(),
                    totalR.coerceIn(0f, 255f).toInt(),
                    totalG.coerceIn(0f, 255f).toInt(),
                    totalB.coerceIn(0f, 255f).toInt()
                )
            }
        }

        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun embossBitmap(originalBitmap: Bitmap?): Bitmap? {
        if (originalBitmap == null) return null

        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        // Матрица тиснения
        val embossMatrix = arrayOf(
            floatArrayOf(-1f, 0f, 0f),
            floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 0f, 0f)
        )

        val tempPixels = pixels.copyOf()

        val startWithPixelIndex = if (editHalf) result.width / 2 else 0

        for (y in 1 until result.height - 1) {
            for (x in startWithPixelIndex + 1 until result.width - 1) {
                var intensity = 0f

                // Считаем только интенсивность (яркость)
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nx = x + dx
                        val ny = y + dy
                        val pixel = tempPixels[ny * result.width + nx]

                        // Переводим цвет в яркость
                        val pixelIntensity = (Color.red(pixel) * 0.299 +
                                Color.green(pixel) * 0.587 +
                                Color.blue(pixel) * 0.114)

                        intensity += (pixelIntensity * embossMatrix[dy + 1][dx + 1]).toFloat()
                    }
                }

                // Сдвигаем в серую гамму +128
                val grayValue = (intensity + 128f).coerceIn(0f, 255f).toInt()

                // Записываем оттенок серого
                pixels[y * result.width + x] = Color.rgb(grayValue, grayValue, grayValue)
            }
        }

        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun contourBitmap(originalBitmap: Bitmap?): Bitmap? {
        if (originalBitmap == null) return null

        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        // Матрица контура из исходника
        val CONTOUR_COEFF = 3
        val contourMatrix = intArrayOf(
            -1 * CONTOUR_COEFF, -1 * CONTOUR_COEFF, -1 * CONTOUR_COEFF,
            -1 * CONTOUR_COEFF, 8 * CONTOUR_COEFF, -1 * CONTOUR_COEFF,
            -1 * CONTOUR_COEFF, -1 * CONTOUR_COEFF, -1 * CONTOUR_COEFF
        )

        val tempPixels = pixels.copyOf()

        val startWithPixelIndex = if (editHalf) result.width / 2 else 0

        for (y in 1 until result.height - 1) {
            for (x in startWithPixelIndex + 1 until result.width - 1) {
                var newR = 0
                var newG = 0
                var newB = 0
                var count = 0

                // Применяем матрицу 3x3
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nx = x + dx
                        val ny = y + dy
                        val pixel = tempPixels[ny * result.width + nx]

                        val matrixValue = contourMatrix[(dy + 1) * 3 + (dx + 1)]

                        newR += Color.red(pixel) * matrixValue
                        newG += Color.green(pixel) * matrixValue
                        newB += Color.blue(pixel) * matrixValue
                        count += matrixValue
                    }
                }

                // Нормализуем и ограничиваем значения
                if (count != 0) {
                    newR /= count
                    newG /= count
                    newB /= count
                }

                newR = newR.coerceIn(0, 255)
                newG = newG.coerceIn(0, 255)
                newB = newB.coerceIn(0, 255)

                pixels[y * result.width + x] = Color.rgb(newR, newG, newB)
            }
        }

        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun histogramCorrectionBitmap(
        originalBitmap: Bitmap,
        offsetBottom: Int,
        offsetTop: Int
    ): Bitmap {
        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(result.width * result.height)
        result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

        // Создаем таблицу преобразования
        val rgbTransTable = Array(3) { IntArray(256) }

        // Инициализация таблицы преобразования
        for (t in 0..2) {
            // Пиксели от 0 до offsetBottom становятся 0
            for (i in 0 until offsetBottom) {
                rgbTransTable[t][i] = 0
            }

            // Пиксели от 255 до 256-offsetTop становятся 255
            for (i in 255 downTo 256 - offsetTop) {
                rgbTransTable[t][i] = 255
            }

            // Растягиваем оставшийся диапазон
            val step = 256.0 / (256 - (offsetBottom + offsetTop))
            var value = 0.0

            for (i in offsetBottom until 256 - offsetTop) {
                rgbTransTable[t][i] = (value + 0.5).toInt()
                value += step
            }
        }

        // Применяем коррекцию только к нужным пикселям
        for (i in pixels.indices) {
            // Если режим "половина" - обрабатываем только правую половину
            if (editHalf) {
                val x = i % originalBitmap.width
                if (x < originalBitmap.width / 2) {
                    continue // Пропускаем левую половину
                }
            }

            val alpha = Color.alpha(pixels[i])
            val red = Color.red(pixels[i])
            val green = Color.green(pixels[i])
            val blue = Color.blue(pixels[i])

            pixels[i] = Color.argb(
                alpha,
                rgbTransTable[0][red],
                rgbTransTable[1][green],
                rgbTransTable[2][blue]
            )
        }

        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }
}