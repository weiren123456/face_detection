package com.ml.shubham0204.facenet_android.presentation.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ml.shubham0204.facenet_android.data.AttendanceRecord
import com.ml.shubham0204.facenet_android.data.ObjectBoxStore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceScreen(
    onNavigateBack: () -> Unit
) {
    var attendanceList by remember { mutableStateOf(listOf<AttendanceRecord>()) }

    LaunchedEffect(Unit) {
        val box = ObjectBoxStore.store.boxFor(AttendanceRecord::class.java)
        attendanceList = box.query().build().find()
            .sortedByDescending { maxOf(it.morningTimestamp, it.afternoonTimestamp) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Attendance List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (attendanceList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance records found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(attendanceList) { record ->
                        AttendanceRow(record)
                        Divider(Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(record: AttendanceRecord) {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val morningTime = if (record.morningChecked) sdf.format(Date(record.morningTimestamp)) else "Not marked"
    val afternoonTime = if (record.afternoonChecked) sdf.format(Date(record.afternoonTimestamp)) else "Not marked"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Name: ${record.personName}", style = MaterialTheme.typography.bodyLarge)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Morning: $morningTime", style = MaterialTheme.typography.bodyMedium)
            if (record.morningChecked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Morning Present",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Afternoon: $afternoonTime", style = MaterialTheme.typography.bodyMedium)
            if (record.afternoonChecked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Afternoon Present",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
