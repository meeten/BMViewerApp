package com.example.bmviewerapp.presentation.image.editor

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bmviewerapp.presentation.image.analysis.AnimatedHistogramView
import com.example.bmviewerapp.presentation.image.analysis.HistogramViewModel
import com.example.bmviewerapp.ui.theme.LightBlue
import com.example.bmviewerapp.ui.theme.SliderBlue
import com.example.bmviewerapp.ui.theme.SliderGray
import com.example.bmviewerapp.ui.theme.SliderThumb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToolContent(
    imageUri: Uri,
    selectedTool: EditTool,
    editHalf: Boolean,
    onImageChanged: (Bitmap?) -> Unit = {}
) {
    val bitmapViewModel: BitmapViewModel = viewModel()
    val histogramViewModel: HistogramViewModel = viewModel()
    val filterStateViewModel: FilterStateViewModel = viewModel()
    val context = LocalContext.current

    val originalBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val previewBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val fastPreviewBitmap = remember { mutableStateOf<Bitmap?>(null) }

    // Флаг для отслеживания активного изменения слайдера
    val isSliderChanging = remember { mutableStateOf(false) }

    // Отдельное состояние для быстрого превью во время движения слайдера
    val fastPreviewResult = remember { mutableStateOf<Bitmap?>(null) }

    // Загружаем оригинал
    LaunchedEffect(imageUri) {
        originalBitmap.value = withContext(Dispatchers.IO) {
            bitmapViewModel.parseBmpFromUri(context, imageUri)
        }
        previewBitmap.value = originalBitmap.value

        originalBitmap.value?.let { bitmap ->
            fastPreviewBitmap.value = bitmapViewModel.createPreviewBitmap(bitmap, scale = 0.3f)
        }
    }

    LaunchedEffect(editHalf) {
        bitmapViewModel.setEditMode(editHalf)
    }

    // БЫСТРЫЙ предпросмотр (активен во время движения слайдера)
    LaunchedEffect(filterStateViewModel.filterParams, fastPreviewBitmap.value, selectedTool) {
        if (fastPreviewBitmap.value == null) return@LaunchedEffect
        if (selectedTool != EditTool.BRIGHTNESS && selectedTool != EditTool.HISTOGRAM) return@LaunchedEffect

        // Помечаем, что слайдер изменяется
        isSliderChanging.value = true

        // Убираем задержку для мгновенного отклика
        val fastResult = withContext(Dispatchers.IO) {
            bitmapViewModel.applyAllFilters(
                filterStateViewModel.filterParams,
                fastPreviewBitmap.value!!
            )
        }

        // Сразу обновляем быстрый превью
        fastPreviewResult.value = fastResult
        previewBitmap.value = fastResult
    }

    // ПОЛНАЯ обработка (после окончания движения слайдера)
    LaunchedEffect(filterStateViewModel.filterParams, originalBitmap.value) {
        if (originalBitmap.value == null) return@LaunchedEffect

        // Если слайдер не изменяется, пропускаем полную обработку
        if (!isSliderChanging.value) return@LaunchedEffect

        // Увеличиваем задержку для определения окончания движения слайдера
        delay(500) // Ждем, пока пользователь не перестанет двигать слайдер

        // Проверяем, что слайдер все еще не изменяется после задержки
        if (!isSliderChanging.value) return@LaunchedEffect

        // Если дошли сюда, значит слайдер перестал изменяться
        isSliderChanging.value = false

        val fullResult = withContext(Dispatchers.IO) {
            bitmapViewModel.applyAllFilters(
                filterStateViewModel.filterParams,
                originalBitmap.value!!
            )
        }

        previewBitmap.value = fullResult
    }

    // Обновляем отображаемое изображение в зависимости от состояния слайдера
    LaunchedEffect(fastPreviewResult.value, isSliderChanging.value) {
        if (isSliderChanging.value && fastPreviewResult.value != null) {
            // Во время движения слайдера показываем быстрый превью
            previewBitmap.value = fastPreviewResult.value
        }
    }

    LaunchedEffect(previewBitmap.value) {
        onImageChanged(previewBitmap.value)
    }

    val histogramData = remember { mutableStateOf<List<Int>>(emptyList()) }
    LaunchedEffect(previewBitmap.value) {
        previewBitmap.value?.let { bitmap ->
            histogramData.value = withContext(Dispatchers.Default) {
                histogramViewModel.calculateHistogram(bitmap)
            }
        }
    }

    val imageBitmap = previewBitmap.value?.asImageBitmap() ?: ImageBitmap(width = 1, height = 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTool) {
                EditTool.HISTOGRAM -> {
                    AnimatedHistogramView(
                        histogramData = histogramData.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = filterStateViewModel.filterParams.histogramOffsetTop,
                            onValueChange = {
                                filterStateViewModel.updateOffsetTop(it)
                            },
                            onValueChangeFinished = {
                                // Принудительно запускаем полную обработку при окончании движения
                                isSliderChanging.value = false
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = SliderThumb,
                                activeTrackColor = SliderBlue,
                                inactiveTrackColor = SliderGray,
                                activeTickColor = SliderBlue,
                                inactiveTickColor = SliderGray
                            )
                        )
                        Text(
                            text = "${filterStateViewModel.filterParams.histogramOffsetTop.toInt()}",
                            modifier = Modifier.width(40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = filterStateViewModel.filterParams.histogramOffsetBottom,
                            onValueChange = {
                                filterStateViewModel.updateOffsetBottom(it)
                            },
                            onValueChangeFinished = {
                                // Принудительно запускаем полную обработку при окончании движения
                                isSliderChanging.value = false
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = SliderThumb,
                                activeTrackColor = SliderBlue,
                                inactiveTrackColor = SliderGray,
                                activeTickColor = SliderBlue,
                                inactiveTickColor = SliderGray
                            )
                        )
                        Text(
                            text = "${filterStateViewModel.filterParams.histogramOffsetBottom.toInt()}",
                            modifier = Modifier.width(40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                EditTool.BRIGHTNESS -> {
                    Text(
                        text = "Brightness",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        fontSize = 16.sp
                    )
                    Slider(
                        value = filterStateViewModel.filterParams.brightness,
                        onValueChange = {
                            filterStateViewModel.updateBrightness(it)
                            isSliderChanging.value = true
                        },
                        onValueChangeFinished = {
                            isSliderChanging.value = false
                        },
                        valueRange = -1.0f..1.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SliderThumb,
                            activeTrackColor = SliderBlue,
                            inactiveTrackColor = SliderGray,
                            activeTickColor = SliderBlue,
                            inactiveTickColor = SliderGray
                        )
                    )

                    Text(
                        text = "Contrast",
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                        fontSize = 16.sp
                    )
                    Slider(
                        value = filterStateViewModel.filterParams.contrast,
                        onValueChange = {
                            filterStateViewModel.updateContrast(it)
                            isSliderChanging.value = true
                        },
                        onValueChangeFinished = {
                            isSliderChanging.value = false
                        },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SliderThumb,
                            activeTrackColor = SliderBlue,
                            inactiveTrackColor = SliderGray,
                            activeTickColor = SliderBlue,
                            inactiveTickColor = SliderGray
                        )
                    )
                }

                EditTool.INVERT_COLORS -> {
                    FilterButton("Invert") {
                        isSliderChanging.value = false
                        previewBitmap.value = bitmapViewModel.invertBitmapColors((originalBitmap.value))
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.SHARP -> {
                    FilterButton("Sharpen") {
                        isSliderChanging.value = false
                        previewBitmap.value = bitmapViewModel.sharpenBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.EMBOSS -> {
                    FilterButton("Emboss") {
                        isSliderChanging.value = false
                        previewBitmap.value = bitmapViewModel.embossBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.CONTOUR -> {
                    FilterButton("Contour") {
                        isSliderChanging.value = false
                        previewBitmap.value = bitmapViewModel.contourBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.BLUR -> {
                    FilterButton("Blur") {
                        isSliderChanging.value = false
                        previewBitmap.value = bitmapViewModel.blurBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }
            }
        }

        Button(
            onClick = {
                val original = bitmapViewModel.parseBmpFromUri(context, imageUri)
                originalBitmap.value = original
                previewBitmap.value = original
                filterStateViewModel.resetAll()
                isSliderChanging.value = false
                fastPreviewResult.value = null
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
        ) {
            Text(
                text = "Reset",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun FilterButton(title: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxHeight()) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
        ) {
            Text(
                text = title,
                fontSize = 20.sp
            )
        }
    }
}