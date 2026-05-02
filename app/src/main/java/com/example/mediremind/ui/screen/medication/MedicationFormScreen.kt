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
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.SurfaceCard
import com.example.mediremind.ui.theme.MediRemindTheme

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
                            onSaveMedication(
                                Medication(
                                    id = existingMedication?.id ?: 0,
                                    name = medicationName.value.ifBlank { "Untitled Medication" },
                                    form = parseMedicationForm(medicationForm.value),
                                    dosage = dosage.value.ifBlank { "Not specified" },
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
