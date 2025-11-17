package com.example.bmviewerapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

class NavigationState(val navHostController: NavHostController) {
    fun navigateToImageEditScreen(imageUri: Uri) {
        navHostController.navigate(Screen.ImageEditorScreen.createRouteWithArgs(imageUri))
    }
}

@Composable
fun rememberNavigationState(
    navHostController: NavHostController = rememberNavController()
): NavigationState {
    return remember { NavigationState(navHostController) }
}