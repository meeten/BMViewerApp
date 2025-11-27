package com.example.bmviewerapp.presentation.image.filemanager

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileManagerViewModel : ViewModel() {

    var saveState by mutableStateOf<SaveState>(SaveState.Initial)
        private set

    fun saveImageToGallery(bitmap: Bitmap?, context: Context) {

        if (bitmap == null) {
            saveState = SaveState.Error("Изображение не найдено")
            return
        }

        viewModelScope.launch {
            try {
                saveState = SaveState.Saving

                val fileName = saveBitmapToMediaStore(bitmap, context)

                saveState = SaveState.Success(fileName)

            } catch (e: Error) {
                saveState = SaveState.Error("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
            }
        }

    }

    private fun saveBitmapToMediaStore(bitmap: Bitmap, context: Context): String {
        // Генерируем уникальное имя файла на основе текущего времени
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "BMViewer_$timeStamp.jpg"

        // Создаем запись в MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)  // Имя файла
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // Тип MIME

            // Для Android 10+ (API 29+) указываем папку назначения
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/BMViewer"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1) // Временно помечаем как "в процессе"
            }
        }

        // Вставляем новую запись в MediaStore и получаем URI
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Не удалось создать файл в галерее")

        // Сохраняем изображение через OutputStream
        context.contentResolver.openOutputStream(uri).use { outputStream ->
            if (outputStream == null) {
                throw Exception("Не удалось открыть файл для записи")
            }

            // Сжимаем Bitmap в JPEG с качеством 95% и сохраняем
            val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)

            if (!success) {
                throw Exception("Ошибка при сжатии изображения")
            }
        }

        // Для Android 10+ снимаем флаг "в процессе"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }

        return fileName
    }

    fun resetSaveState() {
        saveState = SaveState.Initial
    }
}