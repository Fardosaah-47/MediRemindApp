package com.example.mediremind.ui.screen.medication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.theme.MediAmber
import com.example.mediremind.ui.theme.MediBlue
import com.example.mediremind.ui.theme.MediCoral
import com.example.mediremind.ui.theme.MediGreen
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediMint
import com.example.mediremind.ui.theme.MediMuted
import com.example.mediremind.ui.theme.MediPurple
import com.example.mediremind.ui.theme.MediSurfaceRaised
import com.example.mediremind.ui.theme.MediTeal
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAmber,
                    contentColor = MediInk
                )
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "MEDICATIONS - ${medications.size} SAVED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MediTeal
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
    val formLabel = medication.form.name.lowercase().replaceFirstChar { it.uppercase() }
    val initials = medication.name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "Rx" }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MediSurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(formColor.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 24.dp, y = (-28).dp)
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(formColor.copy(alpha = 0.14f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-24).dp, y = 30.dp)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MediAmber.copy(alpha = 0.14f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill(
                        label = formLabel,
                        containerColor = Color.White.copy(alpha = 0.84f),
                        contentColor = formColor
                    )
                    if (isLowStock) {
                        Pill(
                            label = "Low stock",
                            containerColor = MediCoral.copy(alpha = 0.14f),
                            contentColor = MediCoral
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MediAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MediInk
                        )
                    }
                    Text(
                        text = if (medication.isQrImported) "QR imported" else "Manual entry",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MediInk,
                        maxLines = 1
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MediInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = medication.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Pill(
                        label = "${medication.currentStockAmount.toInt()} ${medication.stockUnit}",
                        containerColor = if (isLowStock) MediCoral.copy(alpha = 0.14f) else MediGreen.copy(alpha = 0.12f),
                        contentColor = if (isLowStock) MediCoral else MediGreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { stockFraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (isLowStock) MediCoral else MediTeal,
                        trackColor = MediMint,
                        strokeCap = StrokeCap.Round
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = formColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            tint = formColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                if (medication.referenceImageUri.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = MediCoral,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reference photo needed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MediCoral
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Pill(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun EmptyMedicationsState(
    modifier: Modifier = Modifier
) {
    Column(
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
        MedicationForm.TABLET -> MediTeal
        MedicationForm.CAPSULE -> MediPurple
        MedicationForm.LIQUID -> MediBlue
        MedicationForm.INJECTION -> MediCoral
        MedicationForm.OTHER -> MediMuted
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
