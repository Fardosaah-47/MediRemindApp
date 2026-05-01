package com.example.mediremind.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    patientName: String? = null,
    medicationCount: Int = 0,
    scheduleCount: Int = 0,
    dueTodayCount: Int = 0,
    loggedTodayCount: Int = 0,
    nextStepLabel: String = "Set up the patient profile first.",
    onStartMedicationFlow: () -> Unit = {},
    onStartScheduleFlow: () -> Unit = {},
    onStartDoseLoggingFlow: () -> Unit = {},
    onStartQrImportFlow: () -> Unit = {},
    onStartProfileFlow: () -> Unit = {},
    onStartPatientReportFlow: () -> Unit = {},
    onStartCaregiverScanFlow: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "MediRemind",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        if (!patientName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Patient: $patientName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Offline medication support for patients, caregivers, and daily dose tracking.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Today at a glance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                DashboardStatRow(
                    leftLabel = "Medications",
                    leftValue = medicationCount.toString(),
                    rightLabel = "Schedules",
                    rightValue = scheduleCount.toString()
                )

                Spacer(modifier = Modifier.height(12.dp))

                DashboardStatRow(
                    leftLabel = "Due Today",
                    leftValue = dueTodayCount.toString(),
                    rightLabel = "Logged Today",
                    rightValue = loggedTodayCount.toString()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Next best step",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = nextStepLabel,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Core flow",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Save the patient profile and medication list.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "2. Set the schedule and verify real doses with a live photo.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "3. Review history, refill status, and caregiver reports.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        HomeActionButton(
            label = "Patient Profile",
            onClick = onStartProfileFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Medication Setup",
            onClick = onStartMedicationFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Schedule Setup",
            onClick = onStartScheduleFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Dose Logging",
            onClick = onStartDoseLoggingFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Import By QR",
            onClick = onStartQrImportFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Patient Report",
            onClick = onStartPatientReportFlow
        )

        Spacer(modifier = Modifier.height(10.dp))

        HomeActionButton(
            label = "Caregiver Scan",
            onClick = onStartCaregiverScanFlow
        )
    }
}

@Composable
private fun DashboardStatRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            label = leftLabel,
            value = leftValue
        )

        Spacer(modifier = Modifier.width(12.dp))

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            label = rightLabel,
            value = rightValue
        )
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HomeActionButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MediRemindTheme {
        HomeScreen()
    }
}
