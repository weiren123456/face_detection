package com.ml.shubham0204.facenet_android.presentation.screens.detect_screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ml.shubham0204.facenet_android.data.RecognitionMetrics
import com.ml.shubham0204.facenet_android.domain.ImageVectorUseCase
import com.ml.shubham0204.facenet_android.domain.PersonUseCase
import org.koin.android.annotation.KoinViewModel

// Define the data class for each recognition result.
import com.ml.shubham0204.facenet_android.domain.ImageVectorUseCase.FaceRecognitionResult


@KoinViewModel
class DetectScreenViewModel(
    val personUseCase: PersonUseCase,
    val imageVectorUseCase: ImageVectorUseCase
) : ViewModel() {

    val faceDetectionMetricsState = mutableStateOf<RecognitionMetrics?>(null)

    // NEW: State variable for face recognition results (including match confidence)
    val faceRecognitionResultState = mutableStateOf<List<FaceRecognitionResult>?>(null)

    // NEW: Function to update the recognition results.
    fun updateRecognitionResults(results: List<FaceRecognitionResult>) {
        faceRecognitionResultState.value = results
    }

    fun getNumPeople(): Long = personUseCase.getCount()
}
