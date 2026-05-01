package com.example.mediremind.ui.screen.reminder

import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.repository.CaregiverMedicationSummary
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun PatientReportScreen(
    modifier: Modifier = Modifier,
    patientName: String = "Patient Not Set",
    caregiverName: String? = null,
    reportDate: String = "2026-04-19",
    adherenceRate: Int = 0,
    totalTaken: Int = 0,
    totalSkipped: Int = 0,
    totalSnoozed: Int = 0,
    totalMissed: Int = 0,
    totalLogged: Int = 0,
    medicationSummaries: List<CaregiverMedicationSummary> = emptyList(),
    qrBitmap: ImageBitmap? = null,
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
            text = "Patient Report",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Share one clean summary with a caregiver after doses have been logged.",
            style = MaterialTheme.typography.bodyMedium
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
                    text = patientName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!caregiverName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Caregiver: $caregiverName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Report date: $reportDate",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReportStatRow(
                    leftLabel = "Adherence",
                    leftValue = "$adherenceRate%",
                    rightLabel = "Logged",
                    rightValue = totalLogged.toString()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportStatRow(
                    leftLabel = "Taken",
                    leftValue = totalTaken.toString(),
                    rightLabel = "Missed",
                    rightValue = totalMissed.toString()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ReportStatRow(
                    leftLabel = "Skipped",
                    leftValue = totalSkipped.toString(),
                    rightLabel = "Snoozed",
                    rightValue = totalSnoozed.toString()
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
                    text = "Caregiver handoff",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Open this screen on the patient phone, then let the caregiver scan the QR in MediRemind.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (medicationSummaries.isNotEmpty()) {
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
                        text = "Medication breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    medicationSummaries.forEachIndexed { index, medication ->
                        MedicationSummaryCard(medication = medication)
                        if (index != medicationSummaries.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
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
                    text = "Caregiver QR",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Caregiver report QR",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No QR is ready yet. Log at least one dose so the caregiver report can be generated.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Return To Dashboard")
        }
    }
}

@Composable
private fun ReportStatRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        ReportStatCard(
            modifier = Modifier.weight(1f),
            label = leftLabel,
            value = leftValue
        )

        Spacer(modifier = Modifier.width(12.dp))

        ReportStatCard(
            modifier = Modifier.weight(1f),
            label = rightLabel,
            value = rightValue
        )
    }
}

@Composable
private fun ReportStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
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
private fun MedicationSummaryCard(
    medication: CaregiverMedicationSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Taken ${medication.taken} | Skipped ${medication.skipped}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Snoozed ${medication.snoozed} | Missed ${medication.missed}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PatientReportScreenPreview() {
    MediRemindTheme {
        PatientReportScreen(
            patientName = "Mary Achieng",
            caregiverName = "Jane Achieng",
            reportDate = "2026-04-19",
            adherenceRate = 83,
            totalTaken = 5,
            totalSkipped = 1,
            totalSnoozed = 0,
            totalMissed = 0,
            totalLogged = 6,
            medicationSummaries = listOf(
                CaregiverMedicationSummary(
                    name = "Paracetamol",
                    taken = 4,
                    skipped = 1,
                    snoozed = 0,
                    missed = 0,
                    totalLogged = 5
                )
            )
        )
    }
}
