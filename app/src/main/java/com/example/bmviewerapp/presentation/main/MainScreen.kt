package com.example.bmviewerapp.presentation.main

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.bmviewerapp.presentation.image.ImageReceivingScreen
import com.example.bmviewerapp.ui.theme.LightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photo Editor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBlue)
            )
        }) {
        ImageReceivingScreen(modifier = Modifier.padding(it)) {
            Log.d("URI_IMAGE_LOG", it.path ?: "is null")
        }
    }
}