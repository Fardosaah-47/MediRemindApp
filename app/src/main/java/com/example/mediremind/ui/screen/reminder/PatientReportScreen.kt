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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.repository.CaregiverMedicationSummary
import com.example.mediremind.ui.components.IconBadge
import com.example.mediremind.ui.components.InfoCard
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.StatusChip
import com.example.mediremind.ui.components.SurfaceCard
import com.example.mediremind.ui.theme.AlertCoral
import com.example.mediremind.ui.theme.ClinicTeal
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

    Scaffold(
        topBar = {
            MediRemindTopBar(
                title = "Patient Report",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SurfaceCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(
                        icon = Icons.Outlined.Summarize,
                        size = 48
                    )
                    Column {
                        Text(
                            text = patientName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share one clean summary with a caregiver after doses have been logged.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!caregiverName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusChip(label = "Caregiver: $caregiverName")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(14.dp)
                    )
                    Text(
                        text = "Report date: $reportDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adherence",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "$adherenceRate%",
                        style = MaterialTheme.typography.labelMedium,
                        color = adherenceColor(adherenceRate)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (adherenceRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = adherenceColor(adherenceRate),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
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

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(title = "Caregiver handoff") {
                Text(
                    text = "Open this screen on the patient phone, then let the caregiver scan the QR in MediRemind.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (medicationSummaries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(text = "Medication breakdown")
                Spacer(modifier = Modifier.height(10.dp))

                medicationSummaries.forEachIndexed { index, medication ->
                    MedicationSummaryCard(medication = medication)
                    if (index != medicationSummaries.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SurfaceCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconBadge(
                        icon = Icons.Outlined.QrCode2,
                        size = 44
                    )
                    Column {
                        Text(
                            text = "Caregiver QR",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The caregiver scans this code from another phone running MediRemind.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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
    }
}

private fun adherenceColor(rate: Int): Color = when {
    rate >= 80 -> ClinicTeal
    rate >= 50 -> Color(0xFFD97706)
    else -> AlertCoral
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
    SurfaceCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(
                icon = Icons.Outlined.Groups,
                size = 40
            )
            Column {
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
