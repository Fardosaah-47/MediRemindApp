package com.example.mediremind.ui.screen.profile

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
fun PatientProfileScreen(modifier: Modifier = Modifier) {
    val fullName = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val condition = remember { mutableStateOf("") }
    val caregiverName = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Patient Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the patient details that MediRemind will use later.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName.value,
            onValueChange = { fullName.value = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = age.value,
            onValueChange = { age.value = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = condition.value,
            onValueChange = { condition.value = it },
            label = { Text("Condition") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = caregiverName.value,
            onValueChange = { caregiverName.value = it },
            label = { Text("Caregiver Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Profile")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PatientProfileScreenPreview() {
    MediRemindTheme {
        PatientProfileScreen()
    }
}
