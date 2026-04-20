package com.example.mediremind.ui.screen.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
            text = "Scan the patient's caregiver QR and open the report in a readable format.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back To Home")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onScanReportQrClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scan Patient QR")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Scanned Report",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (scannedReportSummary != null) {
                    Text(text = "Patient: ${scannedReportSummary.patientName}")
                    if (!scannedReportSummary.caregiverName.isNullOrBlank()) {
                        Text(text = "Caregiver: ${scannedReportSummary.caregiverName}")
                    }
                    Text(text = "Date: ${scannedReportSummary.reportDate}")
                    Text(text = "Adherence: ${scannedReportSummary.adherenceRate}%")
                    Text(text = "Taken: ${scannedReportSummary.totalTaken}")
                    Text(text = "Skipped: ${scannedReportSummary.totalSkipped}")
                    Text(text = "Snoozed: ${scannedReportSummary.totalSnoozed}")
                    if (scannedReportSummary.totalMissed > 0) {
                        Text(text = "Missed: ${scannedReportSummary.totalMissed}")
                    }
                    Text(text = "Total Logged: ${scannedReportSummary.totalLogged}")

                    if (scannedReportSummary.medicationSummaries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        scannedReportSummary.medicationSummaries.forEach { medication ->
                            MedicationBreakdownLine(medication = medication)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
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
private fun MedicationBreakdownLine(
    medication: CaregiverMedicationSummary
) {
    Text(
        text = medication.name,
        style = MaterialTheme.typography.titleSmall
    )
    Text(
        text = "Taken ${medication.taken} - Skipped ${medication.skipped} - Snoozed ${medication.snoozed} - Missed ${medication.missed}",
        style = MaterialTheme.typography.bodySmall
    )
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
