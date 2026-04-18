package com.example.mediremind.ui.screen.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.Medication
import com.example.mediremind.ui.theme.MediRemindTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DoseScheduleFormScreen(
    modifier: Modifier = Modifier,
    medications: List<Medication> = emptyList(),
    onSaveSchedules: (List<DoseSchedule>) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val selectedMedicationId = remember { mutableStateOf<Long?>(null) }
    val selectedFrequency = remember { mutableStateOf(DoseFrequency.ONCE_DAILY) }
    val firstReminderTime = remember { mutableStateOf("") }
    val secondReminderTime = remember { mutableStateOf("") }
    val thirdReminderTime = remember { mutableStateOf("") }
    val medicationMenuExpanded = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val selectedMedicationName = medications.firstOrNull { it.id == selectedMedicationId.value }?.name
        ?: "Tap to choose medication"
    val firstTimeLabel = firstTimeLabel(selectedFrequency.value)
    val secondTimeLabel = secondTimeLabel(selectedFrequency.value)
    val thirdTimeLabel = thirdTimeLabel(selectedFrequency.value)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dose Schedule",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set when the patient should take each medication.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Medication",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (medications.isEmpty()) {
            Text(
                text = "Save at least one medication first before creating a schedule.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            ExposedDropdownMenuBox(
                expanded = medicationMenuExpanded.value,
                onExpandedChange = { medicationMenuExpanded.value = !medicationMenuExpanded.value }
            ) {
                OutlinedTextField(
                    value = selectedMedicationName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Medication") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = medicationMenuExpanded.value)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = medicationMenuExpanded.value,
                    onDismissRequest = { medicationMenuExpanded.value = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    medications.forEach { medication ->
                        DropdownMenuItem(
                            text = { Text(medication.name) },
                            onClick = {
                                selectedMedicationId.value = medication.id
                                medicationMenuExpanded.value = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose Frequency",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        DoseFrequency.entries.forEach { frequency ->
            val isSelected = selectedFrequency.value == frequency
            val label = frequency.name.lowercase().replace('_', ' ')
            if (isSelected) {
                Button(
                    onClick = { selectedFrequency.value = frequency },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = label)
                }
            } else {
                OutlinedButton(
                    onClick = { selectedFrequency.value = frequency },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = label)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                openTimePicker(
                    context = context,
                    initialValue = firstReminderTime.value
                ) { selectedTime ->
                    firstReminderTime.value = selectedTime
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (firstReminderTime.value.isBlank()) {
                    "Pick $firstTimeLabel"
                } else {
                    "$firstTimeLabel: ${firstReminderTime.value}"
                }
            )
        }

        if (selectedFrequency.value == DoseFrequency.TWICE_DAILY ||
            selectedFrequency.value == DoseFrequency.THREE_TIMES_DAILY
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    openTimePicker(
                        context = context,
                        initialValue = secondReminderTime.value
                    ) { selectedTime ->
                        secondReminderTime.value = selectedTime
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (secondReminderTime.value.isBlank()) {
                        "Pick $secondTimeLabel"
                    } else {
                        "$secondTimeLabel: ${secondReminderTime.value}"
                    }
                )
            }
        }

        if (selectedFrequency.value == DoseFrequency.THREE_TIMES_DAILY) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    openTimePicker(
                        context = context,
                        initialValue = thirdReminderTime.value
                    ) { selectedTime ->
                        thirdReminderTime.value = selectedTime
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (thirdReminderTime.value.isBlank()) {
                        "Pick $thirdTimeLabel"
                    } else {
                        "$thirdTimeLabel: ${thirdReminderTime.value}"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val medicationId = selectedMedicationId.value
                if (medicationId != null) {
                    val times = buildList {
                        if (firstReminderTime.value.isNotBlank()) add(firstReminderTime.value)
                        if (secondReminderTime.value.isNotBlank()) add(secondReminderTime.value)
                        if (thirdReminderTime.value.isNotBlank()) add(thirdReminderTime.value)
                    }

                    val schedules = times.map { time ->
                        DoseSchedule(
                            medicationId = medicationId,
                            time = time,
                            frequency = selectedFrequency.value
                        )
                    }

                    if (schedules.isNotEmpty()) {
                        onSaveSchedules(schedules)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Schedule")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cancel")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DoseScheduleFormScreenPreview() {
    MediRemindTheme {
        DoseScheduleFormScreen()
    }
}

private fun openTimePicker(
    context: android.content.Context,
    initialValue: String,
    onTimeSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val initialHourMinute = parseHourMinute(initialValue)
    val hour = initialHourMinute?.first ?: calendar.get(Calendar.HOUR_OF_DAY)
    val minute = initialHourMinute?.second ?: calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(formatTime(selectedHour, selectedMinute))
        },
        hour,
        minute,
        false
    ).show()
}

private fun parseHourMinute(value: String): Pair<Int, Int>? {
    if (value.isBlank()) return null

    return try {
        val parser = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = parser.parse(value) ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
    } catch (_: Exception) {
        null
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
}

private fun firstTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.ONCE_DAILY -> "Daily Reminder Time"
        DoseFrequency.TWICE_DAILY -> "Morning Time"
        DoseFrequency.THREE_TIMES_DAILY -> "Morning Time"
        DoseFrequency.WEEKLY -> "Weekly Reminder Time"
        DoseFrequency.AS_NEEDED -> "Reminder Time"
    }
}

private fun secondTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.TWICE_DAILY -> "Evening Time"
        DoseFrequency.THREE_TIMES_DAILY -> "Afternoon Time"
        else -> "Second Reminder Time"
    }
}

private fun thirdTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.THREE_TIMES_DAILY -> "Night Time"
        else -> "Third Reminder Time"
    }
}
