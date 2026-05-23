package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskAdded: (title: String, desc: String, points: Int, isFlexible: Boolean, time: String?) -> Unit,
    strings: com.example.ui.AppStrings
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var pointsStr by remember { mutableStateOf("0") }

    
    // Date selection
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    
    // Time selection
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTimeStr by remember { mutableStateOf<String?>(null) } // HH:mm
    
    // Repeat
    var expandedRepeat by remember { mutableStateOf(false) }
    var repeatOption by remember { mutableStateOf("None") }
    val repeatOptions = listOf("None", "Every Day", "Every Monday", "Every Tuesday", "Every Wednesday", "Every Thursday", "Every Friday", "Every Saturday", "Every Sunday")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    selectedTimeStr = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(strings.cancel) }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(strings.add) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(strings.description) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = pointsStr, onValueChange = { pointsStr = it.filter { ch -> ch.isDigit() } }, label = { Text("Points") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val dStr = if (selectedDateMillis != null) {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis!!))
                    } else strings.dateOptional
                    TextButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dStr)
                    }
                    if (selectedDateMillis != null) {
                        IconButton(onClick = { selectedDateMillis = null }) {
                           Icon(Icons.Default.Close, null)
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val tStr = selectedTimeStr ?: strings.timeOptional
                    TextButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tStr)
                    }
                    if (selectedTimeStr != null) {
                        IconButton(onClick = { selectedTimeStr = null }) {
                           Icon(Icons.Default.Close, null)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    val prefix = if (strings.dashboard == "Головна") "Повторення" else "Repeat"
                    val displayOpt = if (repeatOption == "None") strings.none else repeatOption
                    OutlinedButton(onClick = { expandedRepeat = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("$prefix: $displayOpt")
                    }
                    DropdownMenu(expanded = expandedRepeat, onDismissRequest = { expandedRepeat = false }) {
                        repeatOptions.forEach { opt ->
                            val dispOpt = if (opt == "None") strings.none else opt
                            DropdownMenuItem(text = { Text(dispOpt) }, onClick = { repeatOption = opt; expandedRepeat = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                var finalTimeStr: String? = null
                val dStr = if (selectedDateMillis != null) SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDateMillis!!)) else null
                
                if (dStr != null || selectedTimeStr != null || repeatOption != "None") {
                    val ds = dStr ?: ""
                    val ts = selectedTimeStr ?: ""
                    finalTimeStr = "$ts $ds".trim()
                    if (repeatOption != "None") {
                        if (finalTimeStr.isNotBlank()) finalTimeStr += "|"
                        finalTimeStr += OptionToCode(repeatOption)
                    }
                }
                // Determine `isFlexible` as false only if either specifically time or date are picked. If completely null, it's flexible.
                val isFlexible = dStr == null && selectedTimeStr == null
                val taskPoints = pointsStr.toIntOrNull() ?: 0
                
                onTaskAdded(title, desc, taskPoints, isFlexible, finalTimeStr)
                onDismissRequest()
            }, enabled = title.isNotBlank()) {
                Text(strings.add)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(strings.cancel)
            }
        }
    )
}

private fun OptionToCode(opt: String): String {
    return when (opt) {
        "Every Day" -> "DAILY"
        "Every Monday" -> "WEEKLY_1"
        "Every Tuesday" -> "WEEKLY_2"
        "Every Wednesday" -> "WEEKLY_3"
        "Every Thursday" -> "WEEKLY_4"
        "Every Friday" -> "WEEKLY_5"
        "Every Saturday" -> "WEEKLY_6"
        "Every Sunday" -> "WEEKLY_7"
        else -> ""
    }
}
