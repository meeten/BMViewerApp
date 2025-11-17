package com.example.bmviewerapp.presentation.image

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageEditorScreen(imageUri: Uri) {
    val viewModel: BitmapViewModel = viewModel()

    val bitmap = viewModel.parseBmpFromUri(LocalContext.current, imageUri)
    val invertBitmap = viewModel.invertBitmapColors(bitmap)
    val imageBitmap = invertBitmap?.asImageBitmap() ?: ImageBitmap(width = 1, height = 1)

    Column(modifier = Modifier.fillMaxSize()) {
        Image(bitmap = imageBitmap, contentDescription = null)
    }
}