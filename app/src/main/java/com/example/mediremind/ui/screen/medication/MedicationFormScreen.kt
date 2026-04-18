package com.example.mediremind.ui.screen.medication

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
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
    val scrollState = rememberScrollState()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if (isEditing) "Edit Medication" else "Add Medication",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isEditing) {
                if (isQrMedication) {
                    "This medication came from a pharmacy QR. Key details stay protected unless you unlock them."
                } else {
                    "Update medication details so reminders and refill tracking stay accurate."
                }
            } else {
                "Enter medication details for reminders and refill tracking."
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isQrMedication) {
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
                        text = "Loaded From Pharmacy QR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (areProtectedQrFieldsReadOnly) {
                            "Name, form, and dosage are locked by default. Stock and refill values can still be updated."
                        } else {
                            "QR protection is temporarily unlocked for this edit."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showQrUnlockConfirmation.value = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !qrFieldsUnlocked.value
                    ) {
                        Text(
                            text = if (qrFieldsUnlocked.value) {
                                "QR Details Unlocked"
                            } else {
                                "Unlock QR Details"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = medicationName.value,
            onValueChange = { medicationName.value = it },
            label = { Text("Medication Name") },
            readOnly = areProtectedQrFieldsReadOnly,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicationForm.value,
            onValueChange = { medicationForm.value = it },
            label = { Text("Medication Form e.g. Tablet, Syrup") },
            readOnly = areProtectedQrFieldsReadOnly,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dosage.value,
            onValueChange = { dosage.value = it },
            label = { Text("Dosage Instructions") },
            readOnly = areProtectedQrFieldsReadOnly,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stockAmount.value,
            onValueChange = { stockAmount.value = it },
            label = { Text("Current Stock Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stockUnit.value,
            onValueChange = { stockUnit.value = it },
            label = { Text("Stock Unit e.g. pills, mL, bottles") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = refillAlertAt.value,
            onValueChange = { refillAlertAt.value = it },
            label = { Text("Refill Alert At") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = "Medication Reference Photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Take a camera photo of the real bottle, blister pack, or medicine so the Taken flow can compare against it.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!referenceImageUri.isNullOrBlank()) {
                    MedicationReferencePhotoPreview(imageUri = referenceImageUri)

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onTakeReferencePhoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (referenceImageUri.isNullOrBlank()) {
                            "Take Reference Photo"
                        } else {
                            "Retake Reference Photo"
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val medication = Medication(
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
                onSaveMedication(medication)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isEditing) "Update Medication" else "Save Medication")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cancel")
        }

        if (existingMedication != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showDeleteConfirmation.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete Medication")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (existingMedication != null && showDeleteConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation.value = false },
            title = {
                Text(text = "Delete medication?")
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete ${existingMedication.name}? This will also remove its linked schedules and dose logs. Proceed with caution."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Why are you deleting it?",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    deleteReasonOptions().forEach { reason ->
                        OutlinedButton(
                            onClick = { selectedDeleteReason.value = reason },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedDeleteReason.value == reason) {
                                    "Selected: $reason"
                                } else {
                                    reason
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
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
                    text = "This medication was set by a pharmacy QR. Are you sure you want to change those details?"
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
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Text(
            text = "Reference photo preview unavailable",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
