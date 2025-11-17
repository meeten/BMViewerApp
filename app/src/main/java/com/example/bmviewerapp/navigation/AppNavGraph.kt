package com.example.bmviewerapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    imageReceivingScreenContent: @Composable () -> Unit,
    imageEditorScreenContent: @Composable (imageUri: Uri) -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.ImageReceivingScreen.route
    ) {
        composable(route = Screen.ImageReceivingScreen.route) {
            imageReceivingScreenContent()
        }
        composable(
            route = Screen.ImageEditorScreen.route,
            arguments = listOf(navArgument(name = Screen.KEY_URI_IMAGE) {
                type = NavType.StringType
            })
        ) {
            val imageUri =
                Screen.ImageEditorScreen.parseRouteWithArgs(
                    it.arguments?.getString(
                        Screen.KEY_URI_IMAGE
                    )
                )

            imageEditorScreenContent(imageUri)
        }
    }
}