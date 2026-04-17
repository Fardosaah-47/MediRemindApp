package com.example.mediremind.ui.screen.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.ui.theme.MediRemindTheme

data class ReminderItem(
    val medicationName: String,
    val reminderTime: String,
    val status: String
)

@Composable
fun ReminderSummaryScreen(
    modifier: Modifier = Modifier,
    reminders: List<ReminderItem> = sampleReminders()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Reminder Summary",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This screen will later help patients and caregivers see upcoming and missed reminders.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(reminders) { reminder ->
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
                            text = reminder.medicationName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Time: ${reminder.reminderTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Status: ${reminder.status}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun sampleReminders(): List<ReminderItem> {
    return listOf(
        ReminderItem(
            medicationName = "Metformin",
            reminderTime = "08:00 AM",
            status = "Upcoming"
        ),
        ReminderItem(
            medicationName = "Amlodipine",
            reminderTime = "09:00 PM",
            status = "Missed"
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ReminderSummaryScreenPreview() {
    MediRemindTheme {
        ReminderSummaryScreen()
    }
}
