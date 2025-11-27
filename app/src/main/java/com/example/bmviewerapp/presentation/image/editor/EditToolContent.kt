package com.example.bmviewerapp.presentation.image.editor

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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

    val originalBitmap =
        remember { mutableStateOf(bitmapViewModel.parseBmpFromUri(context, imageUri)) }
    val previewBitmap = remember { mutableStateOf(originalBitmap.value) }

    LaunchedEffect(editHalf) {
        bitmapViewModel.setEditMode(editHalf)
    }

    LaunchedEffect(previewBitmap.value) {
        onImageChanged(previewBitmap.value)
    }

    LaunchedEffect(
        filterStateViewModel.filterParams,
        originalBitmap.value
    ) {
        previewBitmap.value = originalBitmap.value?.let { original ->
            val result = bitmapViewModel.applyAllFilters(
                filterParams = filterStateViewModel.filterParams,
                originalBitmap = original
            )

            result
        }
    }

    val histogramData = remember { mutableStateOf<List<Int>>(emptyList()) }
    LaunchedEffect(previewBitmap.value) {
        previewBitmap.value?.let { bitmap ->
            histogramData.value = histogramViewModel.calculateHistogram(bitmap)
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
                        onValueChange = { filterStateViewModel.updateBrightness(it) },
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
                        onValueChange = { filterStateViewModel.updateContrast(it) },
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
                        previewBitmap.value =
                            bitmapViewModel.invertBitmapColors((originalBitmap.value))
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.SHARP -> {
                    FilterButton("Sharpen") {
                        previewBitmap.value =
                            bitmapViewModel.sharpenBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.EMBOSS -> {
                    FilterButton("Emboss") {
                        previewBitmap.value = bitmapViewModel.embossBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.CONTOUR -> {
                    FilterButton("Contour") {
                        previewBitmap.value =
                            bitmapViewModel.contourBitmap(originalBitmap.value)
                        originalBitmap.value = previewBitmap.value
                    }
                }

                EditTool.BLUR -> {
                    FilterButton("Blur") {
                        previewBitmap.value =
                            bitmapViewModel.blurBitmap(originalBitmap.value)
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