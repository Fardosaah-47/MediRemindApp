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
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun MedicationFormScreen(modifier: Modifier = Modifier) {
    val medicationName = remember { mutableStateOf("") }
    val dosage = remember { mutableStateOf("") }
    val stockCount = remember { mutableStateOf("") }
    val refillThreshold = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Add Medication",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter medication details for reminders and refill tracking.",
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
            value = dosage.value,
            onValueChange = { dosage.value = it },
            label = { Text("Dosage") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = stockCount.value,
            onValueChange = { stockCount.value = it },
            label = { Text("Current Stock") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = refillThreshold.value,
            onValueChange = { refillThreshold.value = it },
            label = { Text("Refill Threshold") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Medication")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationFormScreenPreview() {
    MediRemindTheme {
        MedicationFormScreen()
    }
}
