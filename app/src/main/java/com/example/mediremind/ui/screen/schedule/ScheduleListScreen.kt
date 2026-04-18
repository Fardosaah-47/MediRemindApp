package com.example.mediremind.ui.screen.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.mediremind.ui.theme.MediRemindTheme

data class ScheduleDisplayItem(
    val medicationName: String,
    val reminderTime: String,
    val frequency: DoseFrequency
)

@Composable
fun ScheduleListScreen(
    modifier: Modifier = Modifier,
    schedules: List<ScheduleDisplayItem> = sampleScheduleDisplayItems(),
    onAddScheduleClick: () -> Unit = {},
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
            text = "These schedules will later drive reminder notifications and dose logging.",
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

        if (schedules.isEmpty()) {
            Text(
                text = "No schedules saved yet. Add one to start building reminder logic.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(schedules) { schedule ->
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
                                text = schedule.medicationName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Time: ${schedule.reminderTime}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Frequency: ${schedule.frequency.name.lowercase().replace('_', ' ')}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun sampleScheduleDisplayItems(): List<ScheduleDisplayItem> {
    return listOf(
        ScheduleDisplayItem("Metformin", "08:00 AM", DoseFrequency.TWICE_DAILY),
        ScheduleDisplayItem("Amlodipine", "09:00 PM", DoseFrequency.ONCE_DAILY)
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleListScreenPreview() {
    MediRemindTheme {
        ScheduleListScreen()
    }
}
