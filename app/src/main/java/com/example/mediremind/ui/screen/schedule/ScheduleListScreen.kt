package com.example.mediremind.ui.screen.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.ui.theme.MediRemindTheme

data class ScheduleTimeDisplayItem(
    val schedule: DoseSchedule,
    val reminderTime: String
)

data class ScheduleDisplayGroup(
    val medicationName: String,
    val frequency: DoseFrequency,
    val periodLabel: String,
    val timeEntries: List<ScheduleTimeDisplayItem>
)

@Composable
fun ScheduleListScreen(
    modifier: Modifier = Modifier,
    scheduleGroups: List<ScheduleDisplayGroup> = sampleScheduleDisplayGroups(),
    onAddScheduleClick: () -> Unit = {},
    onScheduleClick: (DoseSchedule) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dose Schedules",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Grouped by medication so repeated daily times stay easier to compare.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onAddScheduleClick) {
            Text(text = "Add New Schedule")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onBackClick) {
            Text(text = "Back To Home")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (scheduleGroups.isEmpty()) {
            Text(
                text = "No schedules saved yet. Add one to start building reminder logic.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(scheduleGroups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = group.medicationName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Frequency: ${group.frequency.name.lowercase().replace('_', ' ')}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Treatment: ${group.periodLabel}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Reminder Times",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            group.timeEntries.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onScheduleClick(entry.schedule) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.reminderTime,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sampleScheduleDisplayGroups(): List<ScheduleDisplayGroup> {
    return listOf(
        ScheduleDisplayGroup(
            medicationName = "Paracetamol",
            frequency = DoseFrequency.THREE_TIMES_DAILY,
            periodLabel = "19 Apr 2026 - 29 Apr 2026",
            timeEntries = listOf(
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 1, medicationId = 1, time = "09:00 AM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "09:00 AM"
                ),
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 2, medicationId = 1, time = "01:00 PM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "01:00 PM"
                ),
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 3, medicationId = 1, time = "09:00 PM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "09:00 PM"
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleListScreenPreview() {
    MediRemindTheme {
        ScheduleListScreen()
    }
}
