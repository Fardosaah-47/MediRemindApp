package com.example.mediremind.ui.screen.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    onSaveMedication: (Medication) -> Unit = {},
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
    val isEditing = existingMedication != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isEditing) "Edit Medication" else "Add Medication",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isEditing) {
                "Update medication details so reminders and refill tracking stay accurate."
            } else {
                "Enter medication details for reminders and refill tracking."
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = medicationName.value,
            onValueChange = { medicationName.value = it },
            label = { Text("Medication Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicationForm.value,
            onValueChange = { medicationForm.value = it },
            label = { Text("Medication Form e.g. Tablet, Syrup") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dosage.value,
            onValueChange = { dosage.value = it },
            label = { Text("Dosage Instructions") },
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
                    refillAlertAt = refillAlertAt.value.toDoubleOrNull() ?: 0.0
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

@Preview(showBackground = true)
@Composable
private fun MedicationFormScreenPreview() {
    MediRemindTheme {
        MedicationFormScreen()
    }
}
