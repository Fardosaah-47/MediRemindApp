package com.example.mediremind.ui.screen.reminder

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.ui.theme.MediRemindTheme

data class DoseLoggingItem(
    val doseScheduleId: Long,
    val medicationId: Long,
    val medicationName: String,
    val scheduledTime: String,
    val frequencyLabel: String
)

data class DoseLogDisplayItem(
    val medicationName: String,
    val scheduledTime: String,
    val status: DoseStatus,
    val takenAt: String?
)

@Composable
fun DoseLoggingScreen(
    modifier: Modifier = Modifier,
    dueDoses: List<DoseLoggingItem> = sampleDoseLoggingItems(),
    recentLogs: List<DoseLogDisplayItem> = sampleDoseLogDisplayItems(),
    onLogDose: (DoseLoggingItem, DoseStatus) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dose Logging",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Record whether each medication was taken, skipped, or snoozed.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back To Home")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Scheduled Doses",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (dueDoses.isEmpty()) {
            Text(
                text = "No schedules found yet. Add a medication schedule first, then return here to log doses.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            dueDoses.forEach { dose ->
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
                            text = dose.medicationName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Scheduled: ${dose.scheduledTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Frequency: ${dose.frequencyLabel}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onLogDose(dose, DoseStatus.TAKEN) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Taken"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onLogDose(dose, DoseStatus.SKIPPED) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Skipped")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onLogDose(dose, DoseStatus.SNOOZED) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Snoozed")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Recent Dose Logs",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (recentLogs.isEmpty()) {
            Text(
                text = "No dose logs yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            recentLogs.forEach { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = log.medicationName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Scheduled: ${log.scheduledTime}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!log.takenAt.isNullOrBlank()) {
                                Text(
                                    text = "Recorded: ${log.takenAt}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Text(
                            text = log.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun sampleDoseLoggingItems(): List<DoseLoggingItem> {
    return listOf(
        DoseLoggingItem(
            doseScheduleId = 1,
            medicationId = 1,
            medicationName = "Paracetamol",
            scheduledTime = "08:00 AM",
            frequencyLabel = "twice daily"
        ),
        DoseLoggingItem(
            doseScheduleId = 2,
            medicationId = 2,
            medicationName = "Cough Syrup",
            scheduledTime = "09:00 PM",
            frequencyLabel = "once daily"
        )
    )
}

private fun sampleDoseLogDisplayItems(): List<DoseLogDisplayItem> {
    return listOf(
        DoseLogDisplayItem(
            medicationName = "Paracetamol",
            scheduledTime = "08:00 AM",
            status = DoseStatus.TAKEN,
            takenAt = "2026-04-18 08:05 AM"
        ),
        DoseLogDisplayItem(
            medicationName = "Cough Syrup",
            scheduledTime = "09:00 PM",
            status = DoseStatus.SNOOZED,
            takenAt = null
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun DoseLoggingScreenPreview() {
    MediRemindTheme {
        DoseLoggingScreen()
    }
}
