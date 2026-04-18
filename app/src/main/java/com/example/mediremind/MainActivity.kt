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
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.repository.DoseScheduleRepository
import com.example.mediremind.data.repository.MedicationRepository
import com.example.mediremind.ui.screen.home.HomeScreen
import com.example.mediremind.ui.screen.medication.MedicationFormScreen
import com.example.mediremind.ui.screen.medication.MedicationListScreen
import com.example.mediremind.ui.screen.schedule.DoseScheduleFormScreen
import com.example.mediremind.ui.screen.schedule.ScheduleDisplayItem
import com.example.mediremind.ui.screen.schedule.ScheduleListScreen
import com.example.mediremind.ui.theme.MediRemindTheme
import kotlinx.coroutines.launch

private enum class AppScreen {
    HOME,
    MEDICATION_LIST,
    MEDICATION_FORM,
    SCHEDULE_LIST,
    SCHEDULE_FORM
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val medicationRepository = remember { MedicationRepository(applicationContext) }
            val doseScheduleRepository = remember { DoseScheduleRepository(applicationContext) }
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
            var schedules by remember { mutableStateOf<List<DoseSchedule>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(currentScreen) {
                if (currentScreen == AppScreen.MEDICATION_LIST) {
                    medications = medicationRepository.getAllMedications()
                }
                if (currentScreen == AppScreen.SCHEDULE_LIST || currentScreen == AppScreen.SCHEDULE_FORM) {
                    medications = medicationRepository.getAllMedications()
                }
                if (currentScreen == AppScreen.SCHEDULE_LIST) {
                    schedules = doseScheduleRepository.getAllDoseSchedules()
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
                        schedules = schedules,
                        onStartMedicationFlow = {
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onStartScheduleFlow = {
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onOpenMedicationForm = {
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onOpenScheduleForm = {
                            currentScreen = AppScreen.SCHEDULE_FORM
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
                        },
                        onCancelScheduleForm = {
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onSaveSchedule = { schedule ->
                            coroutineScope.launch {
                                doseScheduleRepository.insertDoseSchedule(schedule)
                                schedules = doseScheduleRepository.getAllDoseSchedules()
                                currentScreen = AppScreen.SCHEDULE_LIST
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
    schedules: List<DoseSchedule>,
    onStartMedicationFlow: () -> Unit,
    onStartScheduleFlow: () -> Unit,
    onOpenMedicationForm: () -> Unit,
    onOpenScheduleForm: () -> Unit,
    onBackHome: () -> Unit,
    onCancelMedicationForm: () -> Unit,
    onSaveMedication: (Medication) -> Unit,
    onCancelScheduleForm: () -> Unit,
    onSaveSchedule: (DoseSchedule) -> Unit
) {
    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                modifier = modifier,
                onStartMedicationFlow = onStartMedicationFlow,
                onStartScheduleFlow = onStartScheduleFlow
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

        AppScreen.SCHEDULE_LIST -> {
            ScheduleListScreen(
                modifier = modifier,
                schedules = schedules.map { schedule ->
                    ScheduleDisplayItem(
                        medicationName = medications.firstOrNull { it.id == schedule.medicationId }?.name
                            ?: "Unknown Medication",
                        reminderTime = schedule.time,
                        frequency = schedule.frequency
                    )
                },
                onAddScheduleClick = onOpenScheduleForm,
                onBackClick = onBackHome
            )
        }

        AppScreen.SCHEDULE_FORM -> {
            DoseScheduleFormScreen(
                modifier = modifier,
                medications = medications,
                onSaveSchedule = onSaveSchedule,
                onCancel = onCancelScheduleForm
            )
        }
    }
}
