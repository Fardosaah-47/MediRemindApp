package com.example.mediremind.ui.screen.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.UserProfile
import com.example.mediremind.ui.components.InitialsAvatar
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.theme.MediRemindTheme

@Composable
fun PatientProfileScreen(
    modifier: Modifier = Modifier,
    existingProfile: UserProfile? = null,
    profiles: List<UserProfile> = emptyList(),
    activePatientId: Long = existingProfile?.id ?: 0L,
    onSaveProfile: (UserProfile) -> Unit = {},
    onSwitchProfile: (UserProfile) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var editingProfile by remember { mutableStateOf(existingProfile) }
    var fullName by remember { mutableStateOf(existingProfile?.fullName.orEmpty()) }
    var age by remember { mutableStateOf(existingProfile?.age?.toString().orEmpty()) }
    var condition by remember { mutableStateOf(existingProfile?.condition.orEmpty()) }
    var caregiverName by remember { mutableStateOf(existingProfile?.caregiverName.orEmpty()) }
    var formMessage by remember { mutableStateOf("") }

    LaunchedEffect(existingProfile?.id) {
        editingProfile = existingProfile
        fullName = existingProfile?.fullName.orEmpty()
        age = existingProfile?.age?.toString().orEmpty()
        condition = existingProfile?.condition.orEmpty()
        caregiverName = existingProfile?.caregiverName.orEmpty()
        formMessage = ""
    }

    val isEditing = editingProfile != null
    val displayName = fullName.ifBlank { "New Patient" }

    Scaffold(
        topBar = {
            MediRemindTopBar(
                title = "Patient Profiles",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        InitialsAvatar(
                            name = displayName,
                            size = 72
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isEditing) displayName else "New Patient",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (isEditing && !editingProfile?.condition.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = editingProfile?.condition.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    if (profiles.isNotEmpty()) {
                        Text(
                            text = "SAVED PATIENTS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            profiles.forEach { profile ->
                                PatientProfileRow(
                                    profile = profile,
                                    isActive = profile.id == activePatientId,
                                    onSwitch = {
                                        onSwitchProfile(profile)
                                        editingProfile = profile
                                        fullName = profile.fullName
                                        age = profile.age?.toString().orEmpty()
                                        condition = profile.condition.orEmpty()
                                        caregiverName = profile.caregiverName.orEmpty()
                                        formMessage = ""
                                    },
                                    onEdit = {
                                        editingProfile = profile
                                        fullName = profile.fullName
                                        age = profile.age?.toString().orEmpty()
                                        condition = profile.condition.orEmpty()
                                        caregiverName = profile.caregiverName.orEmpty()
                                        formMessage = ""
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                editingProfile = null
                                fullName = ""
                                age = ""
                                condition = ""
                                caregiverName = ""
                                formMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Add Another Patient")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = "PATIENT DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = "Full Name",
                                icon = Icons.Outlined.Person
                            )
                            ProfileField(
                                value = age,
                                onValueChange = { age = it },
                                label = "Age",
                                icon = Icons.Outlined.Cake
                            )
                            ProfileField(
                                value = condition,
                                onValueChange = { condition = it },
                                label = "Condition / Diagnosis",
                                icon = Icons.Outlined.FavoriteBorder
                            )
                            ProfileField(
                                value = caregiverName,
                                onValueChange = { caregiverName = it },
                                label = "Caregiver Name",
                                icon = Icons.Outlined.Badge
                            )
                        }
                    }

                    if (formMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = formMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                formMessage = "Please enter the patient's name before saving."
                            } else {
                                formMessage = ""
                                onSaveProfile(
                                    UserProfile(
                                        id = editingProfile?.id ?: 0,
                                        fullName = fullName.trim(),
                                        age = age.toIntOrNull(),
                                        condition = condition.trim().ifBlank { null },
                                        caregiverName = caregiverName.trim().ifBlank { null }
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (isEditing) "Update Profile" else "Save Profile",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientProfileRow(
    profile: UserProfile,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSwitch),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InitialsAvatar(name = profile.fullName, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = profile.condition ?: "No condition added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
            AssistChip(
                onClick = onSwitch,
                label = { Text(if (isActive) "Active" else "Use") }
            )
            OutlinedButton(onClick = onEdit) {
                Text("Edit")
            }
        }
    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        singleLine = true
    )
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
