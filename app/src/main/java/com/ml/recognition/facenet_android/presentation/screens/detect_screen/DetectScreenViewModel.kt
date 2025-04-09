package com.ml.shubham0204.facenet_android.presentation.screens.detect_screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ml.shubham0204.facenet_android.data.RecognitionMetrics
import com.ml.shubham0204.facenet_android.domain.ImageVectorUseCase
import com.ml.shubham0204.facenet_android.domain.PersonUseCase
import org.koin.android.annotation.KoinViewModel
import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ml.shubham0204.facenet_android.data.AttendanceRecord
import com.ml.shubham0204.facenet_android.data.ObjectBoxStore
import java.text.SimpleDateFormat
import java.util.*


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
    fun recognizeFaceFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val (metrics, recognitionResults) = imageVectorUseCase.getNearestPersonName(bitmap)

            faceDetectionMetricsState.value = metrics
            faceRecognitionResultState.value = recognitionResults
        }
    }

    fun markAttendance(recognizedPersonId: Long) {
        // Get the attendance box from ObjectBoxStore (using the "store" property)
        val attendanceBox = ObjectBoxStore.store.boxFor(AttendanceRecord::class.java)

        // Format the current date, for example "2025-04-08"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Create an attendance record
        val attendanceRecord = AttendanceRecord(
            studentId = recognizedPersonId,
            date = today,
            timestamp = System.currentTimeMillis()
        )

        // Save the record
        attendanceBox.put(attendanceRecord)
    }
    fun getNumPeople(): Long = personUseCase.getCount()
}
