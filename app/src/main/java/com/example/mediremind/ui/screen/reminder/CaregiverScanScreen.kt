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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.repository.CaregiverMedicationSummary
import com.example.mediremind.data.repository.CaregiverReportSummary
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
fun CaregiverScanScreen(
    modifier: Modifier = Modifier,
    scannedReportSummary: CaregiverReportSummary? = null,
    scannedReportMessage: String = "Scan a MediRemind caregiver QR to view a readable report here.",
    onScanReportQrClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            MediRemindTopBar(
                title = "Caregiver Scan",
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
            InfoCard(title = "How this works") {
                Text(
                    text = "Open the patient report on the patient phone, scan the QR here, then review the summary below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onScanReportQrClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Scan Patient Report QR")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (scannedReportSummary != null) {
                SurfaceCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(
                            icon = Icons.Outlined.Summarize,
                            size = 48
                        )
                        Column {
                            Text(
                                text = scannedReportSummary.patientName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (!scannedReportSummary.caregiverName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                StatusChip(label = "Caregiver: ${scannedReportSummary.caregiverName}")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.height(14.dp)
                                )
                                Text(
                                    text = "Report date: ${scannedReportSummary.reportDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                            text = "${scannedReportSummary.adherenceRate}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = caregiverRateColor(scannedReportSummary.adherenceRate)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (scannedReportSummary.adherenceRate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = caregiverRateColor(scannedReportSummary.adherenceRate),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
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

                if (scannedReportSummary.medicationSummaries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionLabel(text = "Medication breakdown")
                    Spacer(modifier = Modifier.height(10.dp))

                    scannedReportSummary.medicationSummaries.forEachIndexed { index, medication ->
                        MedicationSummaryCard(medication = medication)
                        if (index != scannedReportSummary.medicationSummaries.lastIndex) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            } else {
                SurfaceCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconBadge(
                            icon = Icons.Outlined.QrCodeScanner,
                            size = 44
                        )
                        Column {
                            Text(
                                text = "Waiting for scan",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = scannedReportMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun caregiverRateColor(rate: Int): Color = when {
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
