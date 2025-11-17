package com.example.bmviewerapp.presentation.image

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageScreen(imageUri: Uri) {
    val viewModel: BitmapViewModel = viewModel()

    val bitmap = viewModel.parseBmpFromUri(LocalContext.current, imageUri)
    val invertBitmap = viewModel.invertBitmapColors(bitmap)
    val imageBitmap = invertBitmap?.asImageBitmap() ?: ImageBitmap(width = 1, height = 1)
}