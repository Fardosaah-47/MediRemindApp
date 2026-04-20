package com.example.mediremind.ui.screen.reminder

import androidx.compose.foundation.Image
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
            text = "This screen prepares a caregiver-ready report from the patient's medication activity.",
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
                    text = "Report Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Patient: $patientName")
                if (!caregiverName.isNullOrBlank()) {
                    Text(text = "Caregiver: $caregiverName")
                }
                Text(text = "Report Date: $reportDate")
                Text(text = "Adherence: $adherenceRate%")
                Text(text = "Taken: $totalTaken")
                Text(text = "Skipped: $totalSkipped")
                Text(text = "Snoozed: $totalSnoozed")
                if (totalMissed > 0) {
                    Text(text = "Missed: $totalMissed")
                }
                Text(text = "Total Logged: $totalLogged")
            }
        }

        if (medicationSummaries.isNotEmpty()) {
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
                        text = "Medication Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    medicationSummaries.forEach { medication ->
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Taken ${medication.taken} - Skipped ${medication.skipped} - Snoozed ${medication.snoozed} - Missed ${medication.missed}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
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
                    text = "Caregiver QR",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A caregiver can scan this QR in MediRemind to open a readable summary.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Caregiver report QR",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No caregiver QR is ready yet. Log at least one dose first.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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
