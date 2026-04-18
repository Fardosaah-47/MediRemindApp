package com.example.mediremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.repository.MedicationRepository
import com.example.mediremind.ui.screen.home.HomeScreen
import com.example.mediremind.ui.screen.medication.MedicationFormScreen
import com.example.mediremind.ui.screen.medication.MedicationListScreen
import com.example.mediremind.ui.theme.MediRemindTheme
import kotlinx.coroutines.launch

private enum class AppScreen {
    HOME,
    MEDICATION_LIST,
    MEDICATION_FORM
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val medicationRepository = remember { MedicationRepository(applicationContext) }
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(currentScreen) {
                if (currentScreen == AppScreen.MEDICATION_LIST) {
                    medications = medicationRepository.getAllMedications()
                }
            }

            MediRemindTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding),
                        currentScreen = currentScreen,
                        medications = medications,
                        onStartMedicationFlow = {
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onOpenMedicationForm = {
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onBackHome = {
                            currentScreen = AppScreen.HOME
                        },
                        onCancelMedicationForm = {
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onSaveMedication = { medication ->
                            coroutineScope.launch {
                                medicationRepository.insertMedication(medication)
                                medications = medicationRepository.getAllMedications()
                                currentScreen = AppScreen.MEDICATION_LIST
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    modifier: Modifier = Modifier,
    currentScreen: AppScreen,
    medications: List<Medication>,
    onStartMedicationFlow: () -> Unit,
    onOpenMedicationForm: () -> Unit,
    onBackHome: () -> Unit,
    onCancelMedicationForm: () -> Unit,
    onSaveMedication: (Medication) -> Unit
) {
    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                modifier = modifier,
                onStartMedicationFlow = onStartMedicationFlow
            )
        }

        AppScreen.MEDICATION_LIST -> {
            MedicationListScreen(
                modifier = modifier,
                medications = medications,
                onAddMedicationClick = onOpenMedicationForm,
                onBackClick = onBackHome
            )
        }

        AppScreen.MEDICATION_FORM -> {
            MedicationFormScreen(
                modifier = modifier,
                onSaveMedication = onSaveMedication,
                onCancel = onCancelMedicationForm
            )
        }
    }
}
