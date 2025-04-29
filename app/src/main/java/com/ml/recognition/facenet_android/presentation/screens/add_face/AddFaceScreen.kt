package com.ml.shubham0204.facenet_android.presentation.screens.add_face

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ml.shubham0204.facenet_android.presentation.components.AppProgressDialog
import com.ml.shubham0204.facenet_android.presentation.components.DelayedVisibility
import com.ml.shubham0204.facenet_android.presentation.components.hideProgressDialog
import com.ml.shubham0204.facenet_android.presentation.components.showProgressDialog
import com.ml.shubham0204.facenet_android.presentation.theme.FaceNetAndroidTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFaceScreen(onNavigateBack: () -> Unit) {
    FaceNetAndroidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Add Faces", style = MaterialTheme.typography.headlineSmall)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Navigate Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                val viewModel: AddFaceScreenViewModel = koinViewModel()
                ScreenUI(viewModel)
                ImageReadProgressDialog(viewModel, onNavigateBack)
            }
        }
    }
}

@Composable
private fun ScreenUI(viewModel: AddFaceScreenViewModel) {
    // image picker launcher
    val pickVisualMediaLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) {
            viewModel.selectedImageURIs.value = it
        }

    // now track both name & plate
    var personName by remember { viewModel.personNameState }
    var busPlate   by remember { viewModel.busPlateState }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    ) {
        // Person name input
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = personName,
            onValueChange = { personName = it },
            label = { Text(text = "Enter the person's name") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Bus plate input
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = busPlate,
            onValueChange = { busPlate = it },
            label = { Text(text = "Enter bus plate") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Only enable once both fields are non-empty
            Button(
                enabled = personName.isNotEmpty() && busPlate.isNotEmpty(),
                onClick = {
                    pickVisualMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.Photo, contentDescription = "Choose photos")
                Text(text = "Choose photos")
            }

            DelayedVisibility(viewModel.selectedImageURIs.value.isNotEmpty()) {
                Button(onClick = { viewModel.addImages() }) {
                    Text(text = "Add to database")
                }
            }
        }

        DelayedVisibility(viewModel.selectedImageURIs.value.isNotEmpty()) {
            Text(
                text = "${viewModel.selectedImageURIs.value.size} image(s) selected",
                style = MaterialTheme.typography.labelSmall
            )
        }

        ImagesGrid(viewModel)
    }
}

@Composable
private fun ImagesGrid(viewModel: AddFaceScreenViewModel) {
    val uris by remember { viewModel.selectedImageURIs }
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(uris) { uri ->
            AsyncImage(model = uri, contentDescription = null)
        }
    }
}

@Composable
private fun ImageReadProgressDialog(
    viewModel: AddFaceScreenViewModel,
    onNavigateBack: () -> Unit
) {
    val isProcessing        by remember { viewModel.isProcessingImages }
    val numImagesProcessed  by remember { viewModel.numImagesProcessed }
    val context             = LocalContext.current

    AppProgressDialog()
    if (isProcessing) {
        showProgressDialog()
    } else {
        if (numImagesProcessed > 0) {
            onNavigateBack()
            Toast.makeText(context, "Added to database", Toast.LENGTH_SHORT).show()
        }
        hideProgressDialog()
    }
}
