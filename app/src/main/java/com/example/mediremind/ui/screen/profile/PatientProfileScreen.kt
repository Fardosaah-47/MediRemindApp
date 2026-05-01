package com.example.mediremind.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.UserProfile
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun PatientProfileScreen(
    modifier: Modifier = Modifier,
    existingProfile: UserProfile? = null,
    onSaveProfile: (UserProfile) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var fullName by remember(existingProfile?.id) { mutableStateOf(existingProfile?.fullName.orEmpty()) }
    var age by remember(existingProfile?.id) {
        mutableStateOf(existingProfile?.age?.toString().orEmpty())
    }
    var condition by remember(existingProfile?.id) {
        mutableStateOf(existingProfile?.condition.orEmpty())
    }
    var caregiverName by remember(existingProfile?.id) {
        mutableStateOf(existingProfile?.caregiverName.orEmpty())
    }
    var formMessage by remember(existingProfile?.id) { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Patient Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Save the patient details that reports and caregiver sharing should use.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = caregiverName,
                    onValueChange = { caregiverName = it },
                    label = { Text("Caregiver Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (formMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (fullName.isBlank()) {
                    formMessage = "Enter the patient name before saving."
                } else {
                    formMessage = ""
                    onSaveProfile(
                        UserProfile(
                            id = existingProfile?.id ?: 0,
                            fullName = fullName.trim(),
                            age = age.toIntOrNull(),
                            condition = condition.trim().ifBlank { null },
                            caregiverName = caregiverName.trim().ifBlank { null }
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (existingProfile == null) "Save Profile" else "Update Profile")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back To Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PatientProfileScreenPreview() {
    MediRemindTheme {
        PatientProfileScreen(
            existingProfile = UserProfile(
                id = 1,
                fullName = "Mary Achieng",
                age = 67,
                condition = "Diabetes",
                caregiverName = "Jane Achieng"
            )
        )
    }
}
