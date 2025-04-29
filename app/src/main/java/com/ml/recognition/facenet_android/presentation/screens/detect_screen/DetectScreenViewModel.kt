package com.ml.shubham0204.facenet_android.presentation.screens.detect_screen

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shubham0204.facenet_android.data.AttendanceRecord
import com.ml.shubham0204.facenet_android.data.AttendanceRecord_
import com.ml.shubham0204.facenet_android.data.ObjectBoxStore
import com.ml.shubham0204.facenet_android.data.RecognitionMetrics
import com.ml.shubham0204.facenet_android.domain.ImageVectorUseCase
import com.ml.shubham0204.facenet_android.domain.PersonUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.text.SimpleDateFormat
import java.util.*

@KoinViewModel
class DetectScreenViewModel(
    app: Application,
    val personUseCase: PersonUseCase,            // public
    val imageVectorUseCase: ImageVectorUseCase   // public
) : AndroidViewModel(app) {

    val faceDetectionMetricsState = mutableStateOf<RecognitionMetrics?>(null)
    val faceRecognitionResultState =
        mutableStateOf<List<ImageVectorUseCase.FaceRecognitionResult>?>(null)

    val attendanceMarked = mutableStateOf(false)
    val attendanceMarkedName = mutableStateOf<String?>(null)

    fun getNumPeople(): Long = personUseCase.getCount()

    fun recognizeFaceFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            val (metrics, recognitionResults) =
                imageVectorUseCase.getNearestPersonName(bitmap)

            faceDetectionMetricsState.value = metrics
            faceRecognitionResultState.value = recognitionResults

            recognitionResults.firstOrNull()?.let { top ->
                val conf = top.confidence ?: 0
                if (conf > 70 && top.personName != "Not recognized") {
                    markAttendance(top.personName)
                }
            }
        }
    }

    fun markAttendance(recognizedPersonName: String) {
        val box = ObjectBoxStore.store.boxFor(AttendanceRecord::class.java)
        val today =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val now = System.currentTimeMillis()
        val session = getCurrentSession()

        val existing = box.query(
            AttendanceRecord_.personName.equal(recognizedPersonName)
                .and(AttendanceRecord_.date.equal(today))
        ).build().findFirst()

        if (existing != null) {
            if (session == "morning" && !existing.morningChecked) {
                existing.morningChecked = true
                existing.morningTimestamp = now
            } else if (session == "afternoon" && !existing.afternoonChecked) {
                existing.afternoonChecked = true
                existing.afternoonTimestamp = now
            }
            existing.lastUpdated = now
            box.put(existing)
        } else {
            val newRec = AttendanceRecord(
                personName = recognizedPersonName,
                date = today,
                morningChecked = session == "morning",
                afternoonChecked = session == "afternoon",
                morningTimestamp = if (session == "morning") now else 0L,
                afternoonTimestamp = if (session == "afternoon") now else 0L,
                lastUpdated = now
            )
            box.put(newRec)
        }

        attendanceMarkedName.value = recognizedPersonName
        attendanceMarked.value = true
    }

    private fun getCurrentSession(): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (h in 5..11) "morning" else "afternoon"
    }
}
