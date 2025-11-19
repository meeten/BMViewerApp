package com.example.bmviewerapp.presentation.main

import androidx.compose.runtime.Composable
import com.example.bmviewerapp.navigation.AppNavGraph
import com.example.bmviewerapp.navigation.rememberNavigationState
import com.example.bmviewerapp.presentation.image.ImageEditorScreen
import com.example.bmviewerapp.presentation.image.ImageReceivingScreen

@Composable
fun MainScreen() {
    val navigationState = rememberNavigationState()

    AppNavGraph(
        navHostController = navigationState.navHostController,
        imageReceivingScreenContent = {
            ImageReceivingScreen { imageUri ->
                navigationState.navigateToImageEditScreen(imageUri)
            }
        },
        imageEditorScreenContent = { imageUri ->
            ImageEditorScreen(imageUri) {
                navigationState.navHostController.popBackStack()
            }
        }
    )
}