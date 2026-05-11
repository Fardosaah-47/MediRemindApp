package com.example.mediremind.ui.screen.medication

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.SurfaceCard
import com.example.mediremind.ui.theme.MediRemindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationFormScreen(
    modifier: Modifier = Modifier,
    existingMedication: Medication? = null,
    referenceImageUri: String? = existingMedication?.referenceImageUri,
    onTakeReferencePhoto: () -> Unit = {},
    onSaveMedication: (Medication) -> Unit = {},
    onDeleteMedication: (Medication) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val medicationName = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.name.orEmpty())
    }
    val medicationForm = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.form?.displayName().orEmpty())
    }
    val dosage = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.dosage.orEmpty())
    }
    val selectedFrequency = remember(existingMedication?.id) {
        mutableStateOf(inferFrequencyFromDosage(existingMedication?.dosage.orEmpty()))
    }
    val frequencyMenuExpanded = remember(existingMedication?.id) {
        mutableStateOf(false)
    }
    val stockAmount = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.currentStockAmount?.toString().orEmpty())
    }
    val stockUnit = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.stockUnit.orEmpty())
    }
    val refillAlertAt = remember(existingMedication?.id) {
        mutableStateOf(existingMedication?.refillAlertAt?.toString().orEmpty())
    }
    val showDeleteConfirmation = remember(existingMedication?.id) {
        mutableStateOf(false)
    }
    val selectedDeleteReason = remember(existingMedication?.id) {
        mutableStateOf<String?>(null)
    }
    val showQrUnlockConfirmation = remember(existingMedication?.id) {
        mutableStateOf(false)
    }
    val qrFieldsUnlocked = remember(existingMedication?.id) {
        mutableStateOf(false)
    }
    val isEditing = existingMedication != null
    val isQrMedication = existingMedication?.isQrImported == true
    val areProtectedQrFieldsReadOnly = isQrMedication && !qrFieldsUnlocked.value

    Scaffold(
        topBar = {
            MediRemindTopBar(
                title = if (isEditing) "Edit Medication" else "Add Medication",
                onBackClick = onCancel
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isQrMedication) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (areProtectedQrFieldsReadOnly) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (areProtectedQrFieldsReadOnly) {
                                    Icons.Outlined.Lock
                                } else {
                                    Icons.Outlined.LockOpen
                                },
                                contentDescription = null,
                                tint = if (areProtectedQrFieldsReadOnly) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (areProtectedQrFieldsReadOnly) {
                                        "Pharmacy QR Protected"
                                    } else {
                                        "QR Protection Unlocked"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (areProtectedQrFieldsReadOnly) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                                Text(
                                    text = if (areProtectedQrFieldsReadOnly) {
                                        "Name, form, and dosage are locked. Stock fields are still editable."
                                    } else {
                                        "All fields are now editable for this session."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (areProtectedQrFieldsReadOnly) {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    }
                                )
                            }
                            if (!qrFieldsUnlocked.value) {
                                TextButton(onClick = { showQrUnlockConfirmation.value = true }) {
                                    Text("Unlock", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel(text = "MEDICATION DETAILS")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormField(
                            value = medicationName.value,
                            onValueChange = { medicationName.value = it },
                            label = "Medication Name",
                            readOnly = areProtectedQrFieldsReadOnly
                        )
                        FormField(
                            value = medicationForm.value,
                            onValueChange = { medicationForm.value = it },
                            label = "Form (e.g. Tablet, Syrup)",
                            readOnly = areProtectedQrFieldsReadOnly
                        )
                        FormField(
                            value = dosage.value,
                            onValueChange = { dosage.value = it },
                            label = "Dosage Instructions",
                            readOnly = areProtectedQrFieldsReadOnly
                        )
                    }
                }
            }

            item {
                SectionLabel(text = "DOSING FREQUENCY")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    ExposedDropdownMenuBox(
                        expanded = frequencyMenuExpanded.value && !areProtectedQrFieldsReadOnly,
                        onExpandedChange = {
                            if (!areProtectedQrFieldsReadOnly) {
                                frequencyMenuExpanded.value = it
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedFrequency.value?.displayLabel() ?: "Choose how often",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("How often is this taken?") },
                            trailingIcon = {
                                if (areProtectedQrFieldsReadOnly) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = frequencyMenuExpanded.value
                                    )
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = frequencyMenuExpanded.value && !areProtectedQrFieldsReadOnly,
                            onDismissRequest = { frequencyMenuExpanded.value = false }
                        ) {
                            DoseFrequency.entries.forEach { frequency ->
                                DropdownMenuItem(
                                    text = { Text(frequency.displayLabel()) },
                                    onClick = {
                                        selectedFrequency.value = frequency
                                        frequencyMenuExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel(text = "STOCK & REFILL")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FormField(
                                value = stockAmount.value,
                                onValueChange = { stockAmount.value = it },
                                label = "Current Stock",
                                modifier = Modifier.weight(1f)
                            )
                            FormField(
                                value = stockUnit.value,
                                onValueChange = { stockUnit.value = it },
                                label = "Unit",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FormField(
                            value = refillAlertAt.value,
                            onValueChange = { refillAlertAt.value = it },
                            label = "Alert when stock reaches"
                        )
                    }
                }
            }

            item {
                SectionLabel(text = "REFERENCE PHOTO")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Take a photo of the real bottle or blister pack. This is used to verify doses during logging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!referenceImageUri.isNullOrBlank()) {
                            MedicationReferencePhotoPreview(imageUri = referenceImageUri)
                        }
                        Button(
                            onClick = onTakeReferencePhoto,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (referenceImageUri.isNullOrBlank()) {
                                    "Take Reference Photo"
                                } else {
                                    "Retake Photo"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val dosageWithFrequency = buildDosageWithFrequency(
                                dosageText = dosage.value,
                                frequency = selectedFrequency.value
                            )
                            onSaveMedication(
                                Medication(
                                    id = existingMedication?.id ?: 0,
                                    name = medicationName.value.ifBlank { "Untitled Medication" },
                                    form = parseMedicationForm(medicationForm.value),
                                    dosage = dosageWithFrequency,
                                    currentStockAmount = stockAmount.value.toDoubleOrNull() ?: 0.0,
                                    stockUnit = stockUnit.value.ifBlank { "units" },
                                    refillAlertAt = refillAlertAt.value.toDoubleOrNull() ?: 0.0,
                                    referenceImageUri = referenceImageUri,
                                    isQrImported = existingMedication?.isQrImported ?: false
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (isEditing) "Update Medication" else "Save Medication",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (existingMedication != null) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmation.value = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Delete Medication",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (existingMedication != null && showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = {
                Text(text = "Delete ${existingMedication.name}?")
            },
            text = {
                Column {
                    Text(
                        text = "This will also remove linked schedules and dose logs. This cannot be undone."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Reason for deletion:",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    deleteReasonOptions().forEach { reason ->
                        OutlinedButton(
                            onClick = { selectedDeleteReason.value = reason },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDeleteReason.value == reason) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(text = reason)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation.value = false
                        selectedDeleteReason.value = null
                        onDeleteMedication(existingMedication)
                    },
                    enabled = selectedDeleteReason.value != null
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation.value = false
                        selectedDeleteReason.value = null
                    }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showQrUnlockConfirmation.value && isQrMedication) {
        AlertDialog(
            onDismissRequest = { showQrUnlockConfirmation.value = false },
            title = {
                Text(text = "Unlock pharmacy details?")
            },
            text = {
                Text(
                    text = "These details were set from a pharmacy QR. Editing them may cause discrepancies with the prescription. Are you sure?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        qrFieldsUnlocked.value = true
                        showQrUnlockConfirmation.value = false
                    }
                ) {
                    Text(text = "Unlock")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showQrUnlockConfirmation.value = false }
                ) {
                    Text(text = "Keep Locked")
                }
            }
        )
    }
}

private fun parseMedicationForm(input: String): MedicationForm {
    return when (input.trim().lowercase()) {
        "tablet", "pill", "pills" -> MedicationForm.TABLET
        "capsule", "capsules" -> MedicationForm.CAPSULE
        "liquid", "syrup" -> MedicationForm.LIQUID
        "injection", "injectable" -> MedicationForm.INJECTION
        else -> MedicationForm.OTHER
    }
}

private fun buildDosageWithFrequency(
    dosageText: String,
    frequency: DoseFrequency?
): String {
    val base = dosageText.trim().ifBlank { "Not specified" }
    val frequencyText = frequency?.dosageText() ?: return base
    val normalizedBase = base.lowercase()

    return if (frequencyText in normalizedBase || frequency.name.lowercase() in normalizedBase) {
        base
    } else {
        "$base, $frequencyText"
    }
}

private fun inferFrequencyFromDosage(dosage: String): DoseFrequency? {
    val normalized = dosage.lowercase()
    return when {
        "three times daily" in normalized || "3 times daily" in normalized || "three_times_daily" in normalized -> DoseFrequency.THREE_TIMES_DAILY
        "twice daily" in normalized || "2 times daily" in normalized || "twice_daily" in normalized -> DoseFrequency.TWICE_DAILY
        "once daily" in normalized || "once_daily" in normalized || "daily" in normalized -> DoseFrequency.ONCE_DAILY
        "weekly" in normalized -> DoseFrequency.WEEKLY
        "as needed" in normalized || "as_needed" in normalized || "when needed" in normalized -> DoseFrequency.AS_NEEDED
        else -> null
    }
}

private fun DoseFrequency.displayLabel(): String {
    return when (this) {
        DoseFrequency.ONCE_DAILY -> "Once daily"
        DoseFrequency.TWICE_DAILY -> "Twice daily"
        DoseFrequency.THREE_TIMES_DAILY -> "Three times daily"
        DoseFrequency.WEEKLY -> "Once a week"
        DoseFrequency.AS_NEEDED -> "As needed"
    }
}

private fun DoseFrequency.dosageText(): String {
    return when (this) {
        DoseFrequency.ONCE_DAILY -> "once daily"
        DoseFrequency.TWICE_DAILY -> "twice daily"
        DoseFrequency.THREE_TIMES_DAILY -> "three times daily"
        DoseFrequency.WEEKLY -> "weekly"
        DoseFrequency.AS_NEEDED -> "as needed"
    }
}

private fun MedicationForm.displayName(): String {
    return name.lowercase().replaceFirstChar { it.uppercase() }
}

private fun deleteReasonOptions(): List<String> {
    return listOf(
        "Duplicate entry",
        "Wrong medication",
        "No longer prescribed"
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = readOnly,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        trailingIcon = if (readOnly) {
            {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            null
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MedicationFormScreenPreview() {
    MediRemindTheme {
        MedicationFormScreen()
    }
}

@Composable
private fun MedicationReferencePhotoPreview(
    imageUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Medication reference photo",
            modifier = modifier
                .size(100.dp)
                .clip(RoundedCornerShape(14.dp))
        )
    } else {
        Text(
            text = "Reference photo preview unavailable",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
