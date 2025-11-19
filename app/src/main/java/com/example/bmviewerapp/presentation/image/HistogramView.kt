package com.example.bmviewerapp.presentation.image

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bmviewerapp.ui.theme.LightBlue

@Composable
fun AnimatedHistogramView(
    histogramData: List<Int>,
    modifier: Modifier = Modifier
) {
    var shouldAnimate by remember { mutableStateOf(false) }

    val animationProgress by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "histogram_animation"
    )

    LaunchedEffect(histogramData) {
        shouldAnimate = false
        shouldAnimate = true
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Image Histogram",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFF8F9FA))
        ) {
            if (histogramData.isEmpty() || histogramData.all { it == 0 }) return@Canvas

            val maxValue = histogramData.maxOrNull()?.toFloat() ?: 1f
            val barWidth = size.width / histogramData.size

            histogramData.forEachIndexed { index, value ->
                val targetHeight = (value.toFloat() / maxValue) * size.height
                val animatedHeight = targetHeight * animationProgress
                val left = index * barWidth
                val top = size.height - animatedHeight

                drawRect(
                    color = LightBlue,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, animatedHeight)
                )
            }

            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )
        }
    }
}