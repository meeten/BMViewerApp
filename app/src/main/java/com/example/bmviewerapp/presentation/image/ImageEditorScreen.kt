package com.example.bmviewerapp.presentation.image

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bmviewerapp.R
import com.example.bmviewerapp.presentation.image.editor.EditTool
import com.example.bmviewerapp.presentation.image.editor.EditToolContent
import com.example.bmviewerapp.presentation.image.filemanager.FileManagerViewModel
import com.example.bmviewerapp.presentation.image.filemanager.SaveState
import com.example.bmviewerapp.ui.theme.LightBlue
import com.example.bmviewerapp.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(imageUri: Uri, onBackClickListener: () -> Unit) {
    val fileManagerViewModel: FileManagerViewModel = viewModel()
    val context = LocalContext.current
    val hostState = remember { SnackbarHostState() }

    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isShowMoreBottomMenu by remember { mutableStateOf(false) }
    var isShowMoreTopMenu by remember { mutableStateOf(false) }
    var selectedTool by remember { mutableStateOf(EditTool.HISTOGRAM) }
    var editHalf by remember { mutableStateOf(false) }

    LaunchedEffect(fileManagerViewModel.saveState) {
        when (val state = fileManagerViewModel.saveState) {
            is SaveState.Success -> {
                hostState.showSnackbar(
                    message = "✅ Изображение сохранено: ${state.fileName}",
                    duration = SnackbarDuration.Short
                )
                fileManagerViewModel.resetSaveState()
            }

            is SaveState.Error -> {
                hostState.showSnackbar(
                    message = "❌ ${state.error}",
                    duration = SnackbarDuration.Long
                )
                fileManagerViewModel.resetSaveState()
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
        // TODO: topBar вынести в отдельную функцию
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Photo Editor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClickListener() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },

                actions = {
                    IconButton(onClick = { isShowMoreTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = isShowMoreTopMenu,
                        onDismissRequest = { isShowMoreTopMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Save Image") },
                            onClick = {
                                fileManagerViewModel.saveImageToGallery(
                                    currentBitmap,
                                    context
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.save),
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                            })

                        DropdownMenuItem(
                            text = { Text("Edit Half") },
                            onClick = {
                                editHalf = !editHalf
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (editHalf) Icons.Filled.Done else Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                            })
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBlue)
            )
        },

        // TODO: bottomBar вынести в отдельную функцию
        bottomBar = {
            BottomAppBar(containerColor = LightBlue) {
                EditTool.entries
                    .filter { it.isMain }
                    .forEach { editTool ->
                        val isSelected = editTool == selectedTool
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTool = editTool },
                            icon = {
                                Image(
                                    painter = painterResource(editTool.iconRes),
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = editTool.title,
                                    color = if (isSelected) NavyBlue else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = LightBlue,
                            )
                        )
                    }

                NavigationBarItem(
                    selected = false,
                    onClick = { isShowMoreBottomMenu = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.more),
                            color = Color.White
                        )
                    }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            DropdownMenu(
                expanded = isShowMoreBottomMenu,
                onDismissRequest = { isShowMoreBottomMenu = false },
                offset = DpOffset(x = (-10).dp, y = (-8).dp)
            ) {
                EditTool.entries
                    .filter { !it.isMain }
                    .forEach { editTool ->
                        DropdownMenuItem(
                            onClick = {
                                selectedTool = editTool
                                isShowMoreBottomMenu = false
                            },
                            text = { Text(text = editTool.title) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(editTool.iconRes),
                                    contentDescription = null
                                )
                            }
                        )
                    }
            }

            EditToolContent(
                imageUri,
                selectedTool,
                editHalf,
            ) { bitmap ->
                currentBitmap = bitmap
            }
        }
    }
}