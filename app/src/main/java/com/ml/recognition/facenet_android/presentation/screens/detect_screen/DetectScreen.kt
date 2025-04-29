package com.ml.shubham0204.facenet_android.presentation.screens.detect_screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.ml.shubham0204.facenet_android.R
import com.ml.shubham0204.facenet_android.presentation.components.AppAlertDialog
import com.ml.shubham0204.facenet_android.presentation.components.DelayedVisibility
import com.ml.shubham0204.facenet_android.presentation.components.FaceDetectionOverlay
import com.ml.shubham0204.facenet_android.presentation.components.createAlertDialog
import com.ml.shubham0204.facenet_android.presentation.theme.FaceNetAndroidTheme
import org.koin.androidx.compose.koinViewModel

private val cameraPermissionStatus = mutableStateOf(false)
private val cameraFacing = mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
private lateinit var cameraPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectScreen(navController: NavController, onOpenFaceListClick: (() -> Unit)) {
    FaceNetAndroidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(),
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        IconButton(onClick = onOpenFaceListClick) {
                            Icon(imageVector = Icons.Default.Face, contentDescription = "Open Face List")
                        }
                        IconButton(
                            onClick = {
                                cameraFacing.intValue =
                                    if (cameraFacing.intValue == CameraSelector.LENS_FACING_BACK)
                                        CameraSelector.LENS_FACING_FRONT
                                    else
                                        CameraSelector.LENS_FACING_BACK
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
                        }
                        IconButton(onClick = { navController.navigate("attendance") }) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "Attendance List")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ScreenUI()
                Button(
                    onClick = { navController.navigate("attendance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("View Attendance List")
                }
            }
        }
    }
}

@Composable
private fun ScreenUI() {
    val viewModel: DetectScreenViewModel = koinViewModel()
    val markedName by viewModel.attendanceMarkedName

    Box {
        Camera(viewModel)

        DelayedVisibility(viewModel.getNumPeople() > 0) {
            val metrics by remember { viewModel.faceDetectionMetricsState }
            val results by remember { viewModel.faceRecognitionResultState }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Recognition on ${viewModel.getNumPeople()} face(s)",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                results?.forEach { result ->
                    Text(
                        text = "Matched: ${result.personName}\n" +
                                "Confidence: ${result.confidence ?: 0}%\n" +
                                "Spoof Score: ${result.spoofResult?.score ?: "N/A"}",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                metrics?.let {
                    Text(
                        text = "face detection: ${it.timeFaceDetection} ms" +
                                "\nface embedding: ${it.timeFaceEmbedding} ms" +
                                "\nvector search: ${it.timeVectorSearch} ms" +
                                "\nspoof detection: ${it.timeFaceSpoofDetection} ms",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        DelayedVisibility(markedName != null) {
            Text(
                text = "✅ Attendance marked for $markedName",
                color = Color.Green,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        DelayedVisibility(viewModel.getNumPeople() == 0L) {
            Text(
                text = "No images in database",
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.Blue, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }

        AppAlertDialog()
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun Camera(viewModel: DetectScreenViewModel) {
    val context = LocalContext.current
    cameraPermissionStatus.value =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    val cameraFacing by remember { cameraFacing }
    val lifecycleOwner = LocalLifecycleOwner.current

    cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            cameraPermissionStatus.value = it
            if (!it) camaraPermissionDialog()
        }

    DelayedVisibility(cameraPermissionStatus.value) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { FaceDetectionOverlay(lifecycleOwner, context, viewModel) },
            update = { it.initializeCamera(cameraFacing) }
        )
    }
    DelayedVisibility(!cameraPermissionStatus.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Allow Camera Permissions\nThe app cannot work without the camera permission.",
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Allow")
            }
        }
    }
}

private fun camaraPermissionDialog() {
    createAlertDialog(
        "Camera Permission",
        "The app couldn't function without the camera permission.",
        "ALLOW",
        "CLOSE",
        onPositiveButtonClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
        onNegativeButtonClick = {
            // TODO: Handle deny camera permission action
        }
    )
}
