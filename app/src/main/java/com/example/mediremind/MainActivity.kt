package com.example.mediremind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
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
import com.example.mediremind.data.model.DoseLog
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.repository.DoseLogRepository
import com.example.mediremind.data.repository.DoseScheduleRepository
import com.example.mediremind.data.repository.MedicationRepository
import com.example.mediremind.data.repository.QrImportParser
import com.example.mediremind.ui.screen.home.HomeScreen
import com.example.mediremind.ui.screen.medication.MedicationFormScreen
import com.example.mediremind.ui.screen.medication.MedicationListScreen
import com.example.mediremind.ui.screen.medication.QrImportScreen
import com.example.mediremind.ui.screen.reminder.DoseLogDisplayItem
import com.example.mediremind.ui.screen.reminder.DoseLoggingItem
import com.example.mediremind.ui.screen.reminder.DoseLoggingScreen
import com.example.mediremind.ui.screen.schedule.DoseScheduleFormScreen
import com.example.mediremind.ui.screen.schedule.ScheduleDisplayItem
import com.example.mediremind.ui.screen.schedule.ScheduleListScreen
import com.example.mediremind.ui.theme.MediRemindTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppScreen {
    HOME,
    MEDICATION_LIST,
    MEDICATION_FORM,
    SCHEDULE_LIST,
    SCHEDULE_FORM,
    DOSE_LOGGING,
    QR_IMPORT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val medicationRepository = remember { MedicationRepository(applicationContext) }
            val doseScheduleRepository = remember { DoseScheduleRepository(applicationContext) }
            val doseLogRepository = remember { DoseLogRepository(applicationContext) }
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
            var schedules by remember { mutableStateOf<List<DoseSchedule>>(emptyList()) }
            var doseLogs by remember { mutableStateOf<List<DoseLog>>(emptyList()) }
            var selectedMedication by remember { mutableStateOf<Medication?>(null) }
            var qrImportMessage by remember {
                mutableStateOf("Scan a hospital or pharmacy QR code to load medication details.")
            }
            var qrRawPreview by remember { mutableStateOf("No QR scanned yet.") }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(currentScreen) {
                if (currentScreen == AppScreen.MEDICATION_LIST) {
                    medications = medicationRepository.getAllMedications()
                }
                if (
                    currentScreen == AppScreen.SCHEDULE_LIST ||
                    currentScreen == AppScreen.SCHEDULE_FORM ||
                    currentScreen == AppScreen.DOSE_LOGGING
                ) {
                    medications = medicationRepository.getAllMedications()
                }
                if (currentScreen == AppScreen.SCHEDULE_LIST || currentScreen == AppScreen.DOSE_LOGGING) {
                    schedules = doseScheduleRepository.getAllDoseSchedules()
                }
                if (currentScreen == AppScreen.DOSE_LOGGING) {
                    doseLogs = doseLogRepository.getAllDoseLogs()
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
                        doseLogs = doseLogs,
                        selectedMedication = selectedMedication,
                        onStartMedicationFlow = {
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onStartScheduleFlow = {
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onStartDoseLoggingFlow = {
                            currentScreen = AppScreen.DOSE_LOGGING
                        },
                        onStartQrImportFlow = {
                            currentScreen = AppScreen.QR_IMPORT
                        },
                        onOpenMedicationForm = {
                            selectedMedication = null
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onOpenMedicationEditor = { medication ->
                            selectedMedication = medication
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onOpenScheduleForm = {
                            currentScreen = AppScreen.SCHEDULE_FORM
                        },
                        onBackHome = {
                            currentScreen = AppScreen.HOME
                        },
                        onCancelMedicationForm = {
                            selectedMedication = null
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onSaveMedication = { medication ->
                            coroutineScope.launch {
                                if (medication.id == 0L) {
                                    medicationRepository.insertMedication(medication)
                                } else {
                                    medicationRepository.updateMedication(medication)
                                }
                                medications = medicationRepository.getAllMedications()
                                selectedMedication = null
                                currentScreen = AppScreen.MEDICATION_LIST
                            }
                        },
                        onCancelScheduleForm = {
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onSaveSchedules = { newSchedules ->
                            coroutineScope.launch {
                                doseScheduleRepository.insertDoseSchedules(newSchedules)
                                schedules = doseScheduleRepository.getAllDoseSchedules()
                                currentScreen = AppScreen.SCHEDULE_LIST
                            }
                        },
                        onBackFromDoseLogging = {
                            currentScreen = AppScreen.HOME
                        },
                        onLogDose = { doseItem, status ->
                            coroutineScope.launch {
                                val timestamp = currentTimestamp()
                                doseLogRepository.insertDoseLog(
                                    DoseLog(
                                        doseScheduleId = doseItem.doseScheduleId,
                                        medicationId = doseItem.medicationId,
                                        scheduledTime = doseItem.scheduledTime,
                                        status = status,
                                        takenAt = if (status == DoseStatus.TAKEN) timestamp else null,
                                        imageUri = null
                                    )
                                )
                                doseLogs = doseLogRepository.getAllDoseLogs()
                            }
                        },
                        qrImportMessage = qrImportMessage,
                        qrRawPreview = qrRawPreview,
                        onBackFromQrImport = {
                            currentScreen = AppScreen.HOME
                        },
                        onScanMedicationQr = {
                            val options = GmsBarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build()
                            val scanner = GmsBarcodeScanning.getClient(this@MainActivity, options)

                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val rawValue = barcode.rawValue
                                    if (rawValue.isNullOrBlank()) {
                                        qrRawPreview = "No readable QR content."
                                        qrImportMessage = "QR scan succeeded, but the code had no readable content."
                                        return@addOnSuccessListener
                                    }

                                    qrRawPreview = rawValue
                                    qrImportMessage = "QR captured. Trying to import..."

                                    coroutineScope.launch {
                                        try {
                                            importQrPayload(
                                                rawValue = rawValue,
                                                medicationRepository = medicationRepository,
                                                doseScheduleRepository = doseScheduleRepository
                                            )
                                            medications = medicationRepository.getAllMedications()
                                            schedules = doseScheduleRepository.getAllDoseSchedules()
                                            qrImportMessage =
                                                "QR import complete. Medication details and schedules were loaded."
                                        } catch (error: Exception) {
                                            qrImportMessage =
                                                error.message ?: "QR import failed. Check the QR format and try again."
                                        }
                                    }
                                }
                                .addOnCanceledListener {
                                    qrRawPreview = "QR scan canceled."
                                    qrImportMessage = "QR scan was canceled."
                                }
                                .addOnFailureListener { error ->
                                    qrRawPreview = "QR scan failed before content was captured."
                                    qrImportMessage =
                                        error.message ?: "QR scan failed. Try again."
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
    doseLogs: List<DoseLog>,
    selectedMedication: Medication?,
    onStartMedicationFlow: () -> Unit,
    onStartScheduleFlow: () -> Unit,
    onStartDoseLoggingFlow: () -> Unit,
    onStartQrImportFlow: () -> Unit,
    onOpenMedicationForm: () -> Unit,
    onOpenMedicationEditor: (Medication) -> Unit,
    onOpenScheduleForm: () -> Unit,
    onBackHome: () -> Unit,
    onCancelMedicationForm: () -> Unit,
    onSaveMedication: (Medication) -> Unit,
    onCancelScheduleForm: () -> Unit,
    onSaveSchedules: (List<DoseSchedule>) -> Unit,
    onBackFromDoseLogging: () -> Unit,
    onLogDose: (DoseLoggingItem, DoseStatus) -> Unit,
    qrImportMessage: String,
    qrRawPreview: String,
    onBackFromQrImport: () -> Unit,
    onScanMedicationQr: () -> Unit
) {
    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                modifier = modifier,
                onStartMedicationFlow = onStartMedicationFlow,
                onStartScheduleFlow = onStartScheduleFlow,
                onStartDoseLoggingFlow = onStartDoseLoggingFlow,
                onStartQrImportFlow = onStartQrImportFlow
            )
        }

        AppScreen.MEDICATION_LIST -> {
            MedicationListScreen(
                modifier = modifier,
                medications = medications,
                onAddMedicationClick = onOpenMedicationForm,
                onMedicationClick = onOpenMedicationEditor,
                onBackClick = onBackHome
            )
        }

        AppScreen.MEDICATION_FORM -> {
            MedicationFormScreen(
                modifier = modifier,
                existingMedication = selectedMedication,
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
                onSaveSchedules = onSaveSchedules,
                onCancel = onCancelScheduleForm
            )
        }

        AppScreen.DOSE_LOGGING -> {
            DoseLoggingScreen(
                modifier = modifier,
                dueDoses = schedules.map { schedule ->
                    DoseLoggingItem(
                        doseScheduleId = schedule.id,
                        medicationId = schedule.medicationId,
                        medicationName = medications.firstOrNull { it.id == schedule.medicationId }?.name
                            ?: "Unknown Medication",
                        scheduledTime = schedule.time,
                        frequencyLabel = schedule.frequency.name.lowercase().replace('_', ' ')
                    )
                },
                recentLogs = doseLogs.map { log ->
                    DoseLogDisplayItem(
                        medicationName = medications.firstOrNull { it.id == log.medicationId }?.name
                            ?: "Unknown Medication",
                        scheduledTime = log.scheduledTime,
                        status = log.status,
                        takenAt = log.takenAt
                    )
                },
                onLogDose = onLogDose,
                onBackClick = onBackFromDoseLogging
            )
        }

        AppScreen.QR_IMPORT -> {
            QrImportScreen(
                modifier = modifier,
                importMessage = qrImportMessage,
                rawScanPreview = qrRawPreview,
                onScanQrClick = onScanMedicationQr,
                onBackClick = onBackFromQrImport
            )
        }
    }
}

private fun currentTimestamp(): String {
    return SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
}

private suspend fun importQrPayload(
    rawValue: String,
    medicationRepository: MedicationRepository,
    doseScheduleRepository: DoseScheduleRepository
) {
    val payload = QrImportParser.parse(rawValue)

    payload.medications.forEach { importedMedication ->
        val medicationId = medicationRepository.insertMedication(importedMedication.medication)

        val schedulesToSave = importedMedication.times.map { time ->
            DoseSchedule(
                medicationId = medicationId,
                time = time,
                frequency = importedMedication.frequency
            )
        }

        doseScheduleRepository.insertDoseSchedules(schedulesToSave)
    }
}
