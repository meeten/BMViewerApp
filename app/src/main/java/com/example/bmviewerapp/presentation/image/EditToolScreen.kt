package com.example.bmviewerapp.presentation.image

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.bmviewerapp.ui.theme.LightBlue
import com.example.bmviewerapp.ui.theme.SliderBlue
import com.example.bmviewerapp.ui.theme.SliderGray
import com.example.bmviewerapp.ui.theme.SliderThumb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToolScreen(imageUri: Uri, selectedTool: EditTool) {
    val viewModel: BitmapViewModel = viewModel()
    val context = LocalContext.current

    val originalBitmap = remember { mutableStateOf(viewModel.parseBmpFromUri(context, imageUri)) }
    val previewBitmap = remember { mutableStateOf(originalBitmap.value) }

    // Добавляем состояние для гистограммы
    val histogramData = remember { mutableStateOf<List<Int>>(emptyList()) }

    // Вычисляем гистограмму при изменении bitmap
    LaunchedEffect(previewBitmap.value) {
        previewBitmap.value?.let { bitmap ->
            histogramData.value = viewModel.calculateHistogram(bitmap)
        }
    }

    val imageBitmap = previewBitmap.value?.asImageBitmap() ?: ImageBitmap(width = 1, height = 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        var brightness by remember { mutableFloatStateOf(0.0f) }
        var contrast by remember { mutableFloatStateOf(1.25f) }

        // Добавляем состояния для коррекции гистограммы
        var offsetBottom by remember { mutableFloatStateOf(0f) }
        var offsetTop by remember { mutableFloatStateOf(0f) }

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
                    // Отображаем гистограмму
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
                            value = offsetBottom,
                            onValueChange = {
                                offsetBottom = it
                                previewBitmap.value = viewModel.histogramCorrectionBitmap(
                                    originalBitmap.value,
                                    offsetBottom.toInt(),
                                    offsetTop.toInt()
                                )
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
                            text = "${offsetBottom.toInt()}",
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
                            value = offsetTop,
                            onValueChange = {
                                offsetTop = it
                                previewBitmap.value = viewModel.histogramCorrectionBitmap(
                                    originalBitmap.value,
                                    offsetBottom.toInt(),
                                    offsetTop.toInt()
                                )
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
                            text = "${offsetTop.toInt()}",
                            modifier = Modifier.width(40.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                EditTool.SHARP -> {
                    LaunchedEffect(Unit) {
                        previewBitmap.value = viewModel.sharpenBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.EMBOSS -> {
                    LaunchedEffect(Unit) {
                        previewBitmap.value = viewModel.embossBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.CONTOUR -> {
                    LaunchedEffect(Unit) {
                        previewBitmap.value = viewModel.contourBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.BRIGHTNESS -> {
                    LaunchedEffect(brightness, contrast) {
                        previewBitmap.value = originalBitmap.value?.let { original ->
                            var result = original
                            if (brightness != 0f) {
                                result = viewModel.brightnessBitmap(result, brightness)
                            }
                            if (contrast != 1.25f) {
                                result = viewModel.contrastBitmap(result, contrast)
                            }
                            result
                        }
                    }

                    Text(
                        text = "Brightness",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        fontSize = 16.sp
                    )
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
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
                        value = contrast,
                        onValueChange = { contrast = it },
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

                EditTool.BLUR -> {
                    LaunchedEffect(Unit) {
                        previewBitmap.value = viewModel.blurBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.INVERT_COLORS -> {
                    LaunchedEffect(Unit) {
                        previewBitmap.value = viewModel.invertBitmapColors(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }
            }
        }

        Button(
            onClick = {
                val original = viewModel.parseBmpFromUri(context, imageUri)
                originalBitmap.value = original
                previewBitmap.value = original
                brightness = 0.0f
                contrast = 1.25f
                offsetBottom = 0f
                offsetTop = 0f
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