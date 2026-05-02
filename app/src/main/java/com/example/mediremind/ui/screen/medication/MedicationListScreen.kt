package com.example.mediremind.ui.screen.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun MedicationListScreen(
    modifier: Modifier = Modifier,
    medications: List<Medication> = sampleMedications(),
    onAddMedicationClick: () -> Unit = {},
    onMedicationClick: (Medication) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            MediRemindTopBar(title = "Medications", onBackClick = onBackClick)
        },
        floatingActionButton = {
            Button(
                onClick = onAddMedicationClick,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Medication", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (medications.isEmpty()) {
            EmptyMedicationsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "${medications.size} medication${if (medications.size != 1) "s" else ""} saved",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(medications) { medication ->
                    MedicationCard(
                        medication = medication,
                        onClick = { onMedicationClick(medication) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit
) {
    val stockFraction = if (medication.refillAlertAt > 0) {
        (medication.currentStockAmount / (medication.refillAlertAt * 5)).coerceIn(0.0, 1.0).toFloat()
    } else {
        1f
    }
    val isLowStock = medication.currentStockAmount <= medication.refillAlertAt
    val formColor = formColor(medication.form)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = formColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = formColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (medication.isQrImported) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode,
                            contentDescription = "QR Imported",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${medication.form.name.lowercase().replaceFirstChar { it.uppercase() }} · ${medication.dosage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { stockFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (isLowStock) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isLowStock) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = "Low stock",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "${medication.currentStockAmount.toInt()} ${medication.stockUnit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLowStock) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                if (medication.referenceImageUri.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reference photo needed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EmptyMedicationsState(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalPharmacy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No medications yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add your first medication to start building reminders and refill tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun formColor(form: MedicationForm): Color {
    return when (form) {
        MedicationForm.TABLET -> MaterialTheme.colorScheme.primary
        MedicationForm.CAPSULE -> Color(0xFF7E57C2)
        MedicationForm.LIQUID -> Color(0xFF0288D1)
        MedicationForm.INJECTION -> MaterialTheme.colorScheme.tertiary
        MedicationForm.OTHER -> Color(0xFF78909C)
    }
}

private fun sampleMedications(): List<Medication> {
    return listOf(
        Medication(
            id = 1,
            name = "Metformin",
            form = MedicationForm.TABLET,
            dosage = "500 mg twice daily",
            currentStockAmount = 18.0,
            stockUnit = "pills",
            refillAlertAt = 6.0,
            isQrImported = true
        ),
        Medication(
            id = 2,
            name = "Amlodipine",
            form = MedicationForm.TABLET,
            dosage = "10 mg every morning",
            currentStockAmount = 9.0,
            stockUnit = "pills",
            refillAlertAt = 3.0
        ),
        Medication(
            id = 3,
            name = "Cough Syrup",
            form = MedicationForm.LIQUID,
            dosage = "10 mL twice daily",
            currentStockAmount = 100.0,
            stockUnit = "mL",
            refillAlertAt = 30.0
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun MedicationListScreenPreview() {
    MediRemindTheme {
        MedicationListScreen()
    }
}
