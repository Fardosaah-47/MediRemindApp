package com.example.mediremind.ui.screen.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun MedicationListScreen(
    modifier: Modifier = Modifier,
    medications: List<Medication> = sampleMedications()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Medication List",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "These medicines will later power reminders, dose logs, and refill alerts.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { }) {
            Text(text = "Add New Medication")
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(medications) { medication ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Form: ${medication.form}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Dosage: ${medication.dosage}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Stock: ${medication.currentStockAmount} ${medication.stockUnit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Refill Alert At: ${medication.refillAlertAt} ${medication.stockUnit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
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
            refillAlertAt = 6.0
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
