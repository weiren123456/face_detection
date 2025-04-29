package com.ml.shubham0204.facenet_android.presentation.screens.add_face

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ml.shubham0204.facenet_android.domain.AppException
import com.ml.shubham0204.facenet_android.domain.ImageVectorUseCase
import com.ml.shubham0204.facenet_android.domain.PersonUseCase
import com.ml.shubham0204.facenet_android.presentation.components.setProgressDialogText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

// ✅ Add for Attendance DB
import com.ml.shubham0204.facenet_android.data.AttendanceRecord
import com.ml.shubham0204.facenet_android.data.ObjectBoxStore
import java.text.SimpleDateFormat
import java.util.*

@KoinViewModel
class AddFaceScreenViewModel(
    private val personUseCase: PersonUseCase,
    private val imageVectorUseCase: ImageVectorUseCase
) : ViewModel() {

    val personNameState: MutableState<String> = mutableStateOf("")
    val selectedImageURIs: MutableState<List<Uri>> = mutableStateOf(emptyList())
    val busPlateState: MutableState<String> = mutableStateOf("") // ✅ Added for bus plate

    val isProcessingImages: MutableState<Boolean> = mutableStateOf(false)
    val numImagesProcessed: MutableState<Int> = mutableIntStateOf(0)

    fun addImages() {
        isProcessingImages.value = true

        CoroutineScope(Dispatchers.Default).launch {
            // 1️⃣ Add the person to Face DB
            val id = personUseCase.addPerson(
                personNameState.value,
                selectedImageURIs.value.size.toLong(),
                busPlateState.value
            )

            // 2️⃣ ✅ Also add person to Attendance DB
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attendanceBox = ObjectBoxStore.store.boxFor(AttendanceRecord::class.java)

            attendanceBox.put(
                AttendanceRecord(
                    personName = personNameState.value,
                    date = today,
                    busPlate = busPlateState.value // ✅ New: Pass bus plate here
                )
            )

            // 3️⃣ Process each selected image
            selectedImageURIs.value.forEach {
                imageVectorUseCase
                    .addImage(id, personNameState.value, it)
                    .onFailure {
                        val errorMessage = (it as AppException).errorCode.message
                        setProgressDialogText(errorMessage)
                    }
                    .onSuccess {
                        numImagesProcessed.value += 1
                        setProgressDialogText("Processed ${numImagesProcessed.value} image(s)")
                    }
            }

            isProcessingImages.value = false
        }
    }
}
