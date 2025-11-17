package com.example.bmviewerapp.navigation

import android.net.Uri
import androidx.core.net.toUri

sealed class Screen(val route: String) {

    companion object {
        const val KEY_URI_IMAGE = "image_editor"
        private const val IMAGE_RECEIVING_ROUTE = "image_receiving"
        private const val IMAGE_EDITOR_ROUTE = "image_editor/{$KEY_URI_IMAGE}"
    }

    object ImageReceivingScreen : Screen(IMAGE_RECEIVING_ROUTE)
    object ImageEditorScreen : Screen(IMAGE_EDITOR_ROUTE) {

        private const val IMAGE_EDITOR_ROUTE_WITH_ARGS = "image_editor"

        fun createRouteWithArgs(imageUri: Uri): String {
            return "$IMAGE_EDITOR_ROUTE_WITH_ARGS/${Uri.encode(imageUri.toString())}"
        }

        fun parseRouteWithArgs(encodedImageUri: String?): Uri {
            return try {
                Uri.decode(encodedImageUri).toUri()
            } catch (e: Exception) {
                Uri.EMPTY
            }
        }
    }
}