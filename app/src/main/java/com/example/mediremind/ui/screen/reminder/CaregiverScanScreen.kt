package com.example.mediremind.ui.screen.reminder

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.repository.CaregiverMedicationSummary
import com.example.mediremind.data.repository.CaregiverReportSummary
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun CaregiverScanScreen(
    modifier: Modifier = Modifier,
    scannedReportSummary: CaregiverReportSummary? = null,
    scannedReportMessage: String = "Scan a MediRemind caregiver QR to view a readable report here.",
    onScanReportQrClick: () -> Unit = {},
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
            text = "Caregiver Scan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan the patient's report QR and open a simple medication summary on this phone.",
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
                    text = "How this works",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "1. Open the patient report on the patient phone.")
                Text(text = "2. Scan the caregiver QR from this screen.")
                Text(text = "3. Review the adherence summary below.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onScanReportQrClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scan Patient Report QR")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Return To Dashboard")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (scannedReportSummary != null) {
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
                        text = scannedReportSummary.patientName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!scannedReportSummary.caregiverName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Caregiver: ${scannedReportSummary.caregiverName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Report date: ${scannedReportSummary.reportDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ReportStatRow(
                        leftLabel = "Adherence",
                        leftValue = "${scannedReportSummary.adherenceRate}%",
                        rightLabel = "Logged",
                        rightValue = scannedReportSummary.totalLogged.toString()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReportStatRow(
                        leftLabel = "Taken",
                        leftValue = scannedReportSummary.totalTaken.toString(),
                        rightLabel = "Missed",
                        rightValue = scannedReportSummary.totalMissed.toString()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ReportStatRow(
                        leftLabel = "Skipped",
                        leftValue = scannedReportSummary.totalSkipped.toString(),
                        rightLabel = "Snoozed",
                        rightValue = scannedReportSummary.totalSnoozed.toString()
                    )
                }
            }

            if (scannedReportSummary.medicationSummaries.isNotEmpty()) {
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

                        scannedReportSummary.medicationSummaries.forEachIndexed { index, medication ->
                            MedicationSummaryCard(medication = medication)
                            if (index != scannedReportSummary.medicationSummaries.lastIndex) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        } else {
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
                        text = "Waiting for scan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scannedReportMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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
private fun CaregiverScanScreenPreview() {
    MediRemindTheme {
        CaregiverScanScreen(
            scannedReportSummary = CaregiverReportSummary(
                patientName = "Mary Achieng",
                caregiverName = "Jane Achieng",
                reportDate = "2026-04-19",
                adherenceRate = 80,
                totalTaken = 4,
                totalSkipped = 1,
                totalSnoozed = 0,
                totalMissed = 1,
                totalLogged = 6,
                medicationSummaries = listOf(
                    CaregiverMedicationSummary(
                        name = "Paracetamol",
                        taken = 3,
                        skipped = 1,
                        snoozed = 0,
                        missed = 1,
                        totalLogged = 5
                    )
                ),
                qrPayload = ""
            )
        )
    }
}
