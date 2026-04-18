package com.example.mediremind.ui.screen.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.Medication
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun DoseScheduleFormScreen(
    modifier: Modifier = Modifier,
    medications: List<Medication> = emptyList(),
    onSaveSchedule: (DoseSchedule) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val medicationName = remember { mutableStateOf("") }
    val reminderTime = remember { mutableStateOf("") }
    val frequency = remember { mutableStateOf("") }

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

        OutlinedTextField(
            value = medicationName.value,
            onValueChange = { medicationName.value = it },
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = reminderTime.value,
            onValueChange = { reminderTime.value = it },
            label = { Text("Reminder Time e.g. 08:00 AM") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = frequency.value,
            onValueChange = { frequency.value = it },
            label = { Text("Frequency e.g. Daily") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val matchedMedication = medications.firstOrNull {
                    it.name.equals(medicationName.value.trim(), ignoreCase = true)
                }

                if (matchedMedication != null) {
                    val schedule = DoseSchedule(
                        medicationId = matchedMedication.id,
                        time = reminderTime.value.ifBlank { "08:00 AM" },
                        frequency = parseDoseFrequency(frequency.value)
                    )
                    onSaveSchedule(schedule)
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

private fun parseDoseFrequency(input: String): DoseFrequency {
    return when (input.trim().lowercase()) {
        "once", "once daily", "daily" -> DoseFrequency.ONCE_DAILY
        "twice", "twice daily" -> DoseFrequency.TWICE_DAILY
        "three times", "three times daily" -> DoseFrequency.THREE_TIMES_DAILY
        "weekly" -> DoseFrequency.WEEKLY
        "as needed", "prn" -> DoseFrequency.AS_NEEDED
        else -> DoseFrequency.ONCE_DAILY
    }
}

@Preview(showBackground = true)
@Composable
private fun DoseScheduleFormScreenPreview() {
    MediRemindTheme {
        DoseScheduleFormScreen()
    }
}
