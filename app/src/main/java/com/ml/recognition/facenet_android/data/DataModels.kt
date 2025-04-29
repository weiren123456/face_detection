package com.ml.shubham0204.facenet_android.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class FaceImageRecord(
    // primary-key of `FaceImageRecord`
    @Id var recordID: Long = 0,

    // personId is derived from `PersonRecord`
    @Index var personID: Long = 0,
    var personName: String = "",

    // the FaceNet-512 model provides a 512-dimensional embedding
    // the FaceNet model provides a 128-dimensional embedding
    @HnswIndex(dimensions = 512) var faceEmbedding: FloatArray = floatArrayOf()
)

@Entity
data class PersonRecord(
    // primary-key
    @Id var personID: Long = 0,
    var personName: String = "",

    // number of images selected by the user
    // under the name of the person
    var numImages: Long = 0,

    // time when the record was added
    var addTime: Long = 0,
    var busPlate: String = ""

)

data class RecognitionMetrics(
    val timeFaceDetection: Long,
    val timeVectorSearch: Long,
    val timeFaceEmbedding: Long,
    val timeFaceSpoofDetection: Long
)
@Entity
data class AttendanceRecord(
    @Id var id: Long = 0,
    var personName: String = "",    // ✅ add this
    var date: String = "",          // ✅ keep this

    var morningChecked: Boolean = false,     // ☀️ Whether morning attendance was marked
    var afternoonChecked: Boolean = false,   // 🌙 Whether afternoon attendance was marked
    var morningTimestamp: Long = 0L,         // ⏰ Optional: when morning was marked
    var afternoonTimestamp: Long = 0L,       // ⏰ Optional: when afternoon was marked

    var lastUpdated: Long = System.currentTimeMillis(), // 🕒 To sort latest attendance  // optional, but useful
    @Index var busPlate: String = ""
)
