package com.example.bmviewerapp.presentation.image.filemanager

sealed class SaveState {

    object Initial : SaveState()
    object Saving : SaveState()
    data class Success(val fileName: String) : SaveState()
    data class Error(val error: String) : SaveState()
}