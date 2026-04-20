package com.example.mediremind

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import com.example.mediremind.data.model.DoseLog
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.data.model.Medication
import com.example.mediremind.data.model.UserProfile
import com.example.mediremind.data.repository.CaregiverReportQrBuilder
import com.example.mediremind.data.repository.CaregiverReportSummary
import com.example.mediremind.data.repository.DoseLogRepository
import com.example.mediremind.data.repository.DoseScheduleRepository
import com.example.mediremind.data.repository.MedicationRepository
import com.example.mediremind.data.repository.QrImportResult
import com.example.mediremind.data.repository.QrImportParser
import com.example.mediremind.data.repository.UserProfileRepository
import com.example.mediremind.ui.screen.home.HomeScreen
import com.example.mediremind.ui.screen.medication.MedicationFormScreen
import com.example.mediremind.ui.screen.medication.MedicationListScreen
import com.example.mediremind.ui.screen.medication.QrImportScreen
import com.example.mediremind.ui.screen.reminder.CaregiverScanScreen
import com.example.mediremind.ui.screen.reminder.DoseLogDisplayItem
import com.example.mediremind.ui.screen.reminder.DoseLoggingItem
import com.example.mediremind.ui.screen.reminder.DoseLoggingScreen
import com.example.mediremind.ui.screen.reminder.PendingDoseVerificationDisplay
import com.example.mediremind.ui.screen.reminder.PatientReportScreen
import com.example.mediremind.ui.screen.reminder.TodayDoseSummaryDisplay
import com.example.mediremind.ui.screen.schedule.DoseScheduleFormScreen
import com.example.mediremind.ui.screen.schedule.ScheduleDisplayGroup
import com.example.mediremind.ui.screen.schedule.ScheduleListScreen
import com.example.mediremind.ui.screen.schedule.ScheduleTimeDisplayItem
import com.example.mediremind.ui.theme.MediRemindTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.File
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AppScreen {
    HOME,
    MEDICATION_LIST,
    MEDICATION_FORM,
    SCHEDULE_LIST,
    SCHEDULE_FORM,
    DOSE_LOGGING,
    QR_IMPORT,
    PATIENT_REPORT,
    CAREGIVER_SCAN
}

private data class PendingDoseVerification(
    val doseItem: DoseLoggingItem,
    val referenceImageUri: String,
    val capturedImageUri: String,
    val isLikelyMatch: Boolean,
    val matchMessage: String,
    val similarityScore: Int
)

private data class DoseAvailability(
    val isActionAllowed: Boolean,
    val message: String
)

private data class PhotoComparisonResult(
    val isLikelyMatch: Boolean,
    val similarityScore: Int,
    val message: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val medicationRepository = remember { MedicationRepository(applicationContext) }
            val doseScheduleRepository = remember { DoseScheduleRepository(applicationContext) }
            val doseLogRepository = remember { DoseLogRepository(applicationContext) }
            val userProfileRepository = remember { UserProfileRepository(applicationContext) }

            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
            var schedules by remember { mutableStateOf<List<DoseSchedule>>(emptyList()) }
            var doseLogs by remember { mutableStateOf<List<DoseLog>>(emptyList()) }
            var selectedMedication by remember { mutableStateOf<Medication?>(null) }
            var selectedSchedule by remember { mutableStateOf<DoseSchedule?>(null) }
            var medicationReferenceImageUri by remember { mutableStateOf<String?>(null) }
            var userProfile by remember { mutableStateOf<UserProfile?>(null) }
            var qrImportMessage by remember {
                mutableStateOf("Scan a hospital or pharmacy QR code to load medication details.")
            }
            var qrRawPreview by remember { mutableStateOf("No QR scanned yet.") }
            var caregiverReportSummary by remember { mutableStateOf<CaregiverReportSummary?>(null) }
            var caregiverReportQr by remember { mutableStateOf<ImageBitmap?>(null) }
            var scannedCaregiverReportSummary by remember { mutableStateOf<CaregiverReportSummary?>(null) }
            var scannedCaregiverReportMessage by remember {
                mutableStateOf("Scan a MediRemind caregiver QR to view a readable report here.")
            }
            var doseLoggingMessage by remember {
                mutableStateOf("")
            }
            var pendingDoseVerification by remember {
                mutableStateOf<PendingDoseVerification?>(null)
            }
            var pendingMedicationReferenceUri by remember { mutableStateOf<Uri?>(null) }
            var pendingTakenDose by remember { mutableStateOf<DoseLoggingItem?>(null) }
            var pendingVerificationUri by remember { mutableStateOf<Uri?>(null) }
            var timeRefreshKey by remember { mutableStateOf(currentMinuteRefreshKey()) }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                while (true) {
                    timeRefreshKey = currentMinuteRefreshKey()
                    delay(30_000)
                }
            }

            val takeMedicationReferencePhoto = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { wasSaved ->
                val capturedUri = pendingMedicationReferenceUri

                if (wasSaved && capturedUri != null) {
                    medicationReferenceImageUri = capturedUri.toString()
                } else if (capturedUri != null) {
                    applicationContext.contentResolver.delete(capturedUri, null, null)
                }

                pendingMedicationReferenceUri = null
            }

            val takeDoseVerificationPhoto = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { wasSaved ->
                val capturedDose = pendingTakenDose
                val capturedUri = pendingVerificationUri
                val expectedMedication = capturedDose?.let { dose ->
                    medications.firstOrNull { it.id == dose.medicationId }
                }
                val referenceImageUri = expectedMedication?.referenceImageUri

                if (wasSaved && capturedDose != null && capturedUri != null && !referenceImageUri.isNullOrBlank()) {
                    val comparisonResult = compareMedicationPhotos(
                        context = applicationContext,
                        referenceImageUri = referenceImageUri,
                        capturedImageUri = capturedUri.toString()
                    )
                    pendingDoseVerification = PendingDoseVerification(
                        doseItem = capturedDose,
                        referenceImageUri = referenceImageUri,
                        capturedImageUri = capturedUri.toString(),
                        isLikelyMatch = comparisonResult.isLikelyMatch,
                        matchMessage = comparisonResult.message,
                        similarityScore = comparisonResult.similarityScore
                    )
                    doseLoggingMessage = if (comparisonResult.isLikelyMatch) {
                        ""
                    } else {
                        comparisonResult.message
                    }
                } else {
                    if (capturedUri != null) {
                        applicationContext.contentResolver.delete(capturedUri, null, null)
                    }
                    doseLoggingMessage = if (referenceImageUri.isNullOrBlank() && capturedDose != null) {
                        "Add a reference photo for ${capturedDose.medicationName} in Medication Setup first."
                    } else {
                        "No photo was saved, so the dose was not marked as taken."
                    }
                }

                pendingTakenDose = null
                pendingVerificationUri = null
            }

            LaunchedEffect(currentScreen, timeRefreshKey) {
                if (currentScreen == AppScreen.MEDICATION_LIST) {
                    medications = medicationRepository.getAllMedications()
                }
                if (
                    currentScreen == AppScreen.SCHEDULE_LIST ||
                    currentScreen == AppScreen.SCHEDULE_FORM ||
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    medications = medicationRepository.getAllMedications()
                }
                if (
                    currentScreen == AppScreen.SCHEDULE_LIST ||
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    schedules = doseScheduleRepository.getAllDoseSchedules()
                }
                if (currentScreen == AppScreen.DOSE_LOGGING || currentScreen == AppScreen.PATIENT_REPORT) {
                    doseLogs = doseLogRepository.getAllDoseLogs()
                }
                if (
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    val loadedSchedules = doseScheduleRepository.getAllDoseSchedules()
                    val loadedDoseLogs = doseLogRepository.getAllDoseLogs()
                    val autoMissedLogs = buildAutoMissedDoseLogs(
                        schedules = loadedSchedules,
                        existingLogs = loadedDoseLogs,
                        todayDate = currentDateOnly()
                    )
                    if (autoMissedLogs.isNotEmpty()) {
                        autoMissedLogs.forEach { doseLogRepository.insertDoseLog(it) }
                        doseLogs = doseLogRepository.getAllDoseLogs()
                    }
                }
                if (currentScreen == AppScreen.PATIENT_REPORT) {
                    val loadedProfile = userProfileRepository.getFirstUserProfile()
                    val loadedMedications = medicationRepository.getAllMedications()
                    val loadedSchedules = doseScheduleRepository.getAllDoseSchedules()
                    val loadedDoseLogs = doseLogRepository.getAllDoseLogs()

                    userProfile = loadedProfile
                    medications = loadedMedications
                    schedules = loadedSchedules
                    doseLogs = loadedDoseLogs

                    val summary = CaregiverReportQrBuilder.buildSummary(
                        userProfile = loadedProfile,
                        medications = loadedMedications,
                        doseLogs = loadedDoseLogs
                    )
                    caregiverReportSummary = summary
                    caregiverReportQr = if (summary.totalLogged > 0) {
                        CaregiverReportQrBuilder.generateQrBitmap(summary.qrPayload).asImageBitmap()
                    } else {
                        null
                    }
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
                        selectedSchedule = selectedSchedule,
                        medicationReferenceImageUri = medicationReferenceImageUri,
                        pendingDoseVerification = pendingDoseVerification,
                        onStartMedicationFlow = {
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onStartScheduleFlow = {
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onStartDoseLoggingFlow = {
                            doseLoggingMessage = ""
                            currentScreen = AppScreen.DOSE_LOGGING
                        },
                        onStartQrImportFlow = {
                            currentScreen = AppScreen.QR_IMPORT
                        },
                        onStartPatientReportFlow = {
                            currentScreen = AppScreen.PATIENT_REPORT
                        },
                        onStartCaregiverScanFlow = {
                            scannedCaregiverReportSummary = null
                            scannedCaregiverReportMessage =
                                "Scan a MediRemind caregiver QR to view a readable report here."
                            currentScreen = AppScreen.CAREGIVER_SCAN
                        },
                        onOpenMedicationForm = {
                            selectedMedication = null
                            medicationReferenceImageUri = null
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onOpenMedicationEditor = { medication ->
                            selectedMedication = medication
                            medicationReferenceImageUri = medication.referenceImageUri
                            currentScreen = AppScreen.MEDICATION_FORM
                        },
                        onOpenScheduleForm = {
                            selectedSchedule = null
                            currentScreen = AppScreen.SCHEDULE_FORM
                        },
                        onOpenScheduleEditor = { schedule ->
                            selectedSchedule = schedule
                            currentScreen = AppScreen.SCHEDULE_FORM
                        },
                        onBackHome = {
                            currentScreen = AppScreen.HOME
                        },
                        onCancelMedicationForm = {
                            selectedMedication = null
                            medicationReferenceImageUri = null
                            currentScreen = AppScreen.MEDICATION_LIST
                        },
                        onTakeMedicationReferencePhoto = {
                            val imageUri = createAppImageUri(
                                context = applicationContext,
                                folderName = "reference_images",
                                filePrefix = "reference"
                            )
                            pendingMedicationReferenceUri = imageUri
                            takeMedicationReferencePhoto.launch(imageUri)
                        },
                        onSaveMedication = { medication ->
                            coroutineScope.launch {
                                val savedMedicationId = if (medication.id == 0L) {
                                    medicationRepository.insertMedication(medication)
                                } else {
                                    medicationRepository.updateMedication(medication)
                                    medication.id
                                }

                                if (savedMedicationId != 0L) {
                                    syncMedicationSchedulesFromMedicationDetails(
                                        medication = medication.copy(id = savedMedicationId),
                                        doseScheduleRepository = doseScheduleRepository
                                    )
                                }
                                medications = medicationRepository.getAllMedications()
                                schedules = doseScheduleRepository.getAllDoseSchedules()
                                selectedMedication = null
                                medicationReferenceImageUri = null
                                currentScreen = AppScreen.MEDICATION_LIST
                            }
                        },
                        onDeleteMedication = { medication ->
                            coroutineScope.launch {
                                medication.referenceImageUri?.let { imageUri ->
                                    applicationContext.contentResolver.delete(
                                        Uri.parse(imageUri),
                                        null,
                                        null
                                    )
                                }
                                medicationRepository.deleteMedication(medication)
                                medications = medicationRepository.getAllMedications()
                                schedules = doseScheduleRepository.getAllDoseSchedules()
                                doseLogs = doseLogRepository.getAllDoseLogs()
                                selectedMedication = null
                                medicationReferenceImageUri = null
                                currentScreen = AppScreen.MEDICATION_LIST
                            }
                        },
                        onCancelScheduleForm = {
                            selectedSchedule = null
                            currentScreen = AppScreen.SCHEDULE_LIST
                        },
                        onSaveSchedules = { newSchedules ->
                            coroutineScope.launch {
                                if (selectedSchedule != null && newSchedules.size == 1) {
                                    doseScheduleRepository.updateDoseSchedule(newSchedules.first())
                                } else {
                                    doseScheduleRepository.insertDoseSchedules(newSchedules)
                                }
                                schedules = doseScheduleRepository.getAllDoseSchedules()
                                selectedSchedule = null
                                currentScreen = AppScreen.SCHEDULE_LIST
                            }
                        },
                        onBackFromDoseLogging = {
                            pendingDoseVerification?.let { verification ->
                                applicationContext.contentResolver.delete(
                                    Uri.parse(verification.capturedImageUri),
                                    null,
                                    null
                                )
                            }
                            pendingDoseVerification = null
                            pendingTakenDose = null
                            pendingVerificationUri = null
                            doseLoggingMessage = ""
                            currentScreen = AppScreen.HOME
                        },
                        onTakeDosePhoto = { doseItem ->
                            val medication = medications.firstOrNull { it.id == doseItem.medicationId }
                            if (medication?.referenceImageUri.isNullOrBlank()) {
                                doseLoggingMessage =
                                    "Add a reference photo for ${doseItem.medicationName} in Medication Setup before marking it as taken."
                            } else {
                                val imageUri = createAppImageUri(
                                    context = applicationContext,
                                    folderName = "verification_images",
                                    filePrefix = "dose"
                                )
                                pendingTakenDose = doseItem
                                pendingVerificationUri = imageUri
                                pendingDoseVerification?.let { verification ->
                                    applicationContext.contentResolver.delete(
                                        Uri.parse(verification.capturedImageUri),
                                        null,
                                        null
                                    )
                                }
                                pendingDoseVerification = null
                                doseLoggingMessage = ""
                                takeDoseVerificationPhoto.launch(imageUri)
                            }
                        },
                        onConfirmTaken = {
                            val verification = pendingDoseVerification
                            if (verification != null) {
                                if (!verification.isLikelyMatch) {
                                    doseLoggingMessage = verification.matchMessage
                                } else {
                                    coroutineScope.launch {
                                        doseLogRepository.insertDoseLog(
                                            DoseLog(
                                                doseScheduleId = verification.doseItem.doseScheduleId,
                                                medicationId = verification.doseItem.medicationId,
                                                scheduledTime = verification.doseItem.scheduledTime,
                                                logDate = currentDateOnly(),
                                                status = DoseStatus.TAKEN,
                                                takenAt = currentTimestamp(),
                                                imageUri = verification.capturedImageUri
                                            )
                                        )
                                        doseLogs = doseLogRepository.getAllDoseLogs()
                                        doseLoggingMessage =
                                            "Taken dose confirmed for ${verification.doseItem.medicationName}."
                                        pendingDoseVerification = null
                                    }
                                }
                            }
                        },
                        onRetakeTakenPhoto = {
                            val verification = pendingDoseVerification
                            if (verification != null) {
                                applicationContext.contentResolver.delete(
                                    Uri.parse(verification.capturedImageUri),
                                    null,
                                    null
                                )
                                val imageUri = createAppImageUri(
                                    context = applicationContext,
                                    folderName = "verification_images",
                                    filePrefix = "dose"
                                )
                                pendingTakenDose = verification.doseItem
                                pendingVerificationUri = imageUri
                                pendingDoseVerification = null
                                doseLoggingMessage = ""
                                takeDoseVerificationPhoto.launch(imageUri)
                            }
                        },
                        onLogDose = { doseItem, status ->
                            coroutineScope.launch {
                                val timestamp = currentTimestamp()
                                doseLogRepository.insertDoseLog(
                                    DoseLog(
                                        doseScheduleId = doseItem.doseScheduleId,
                                        medicationId = doseItem.medicationId,
                                        scheduledTime = doseItem.scheduledTime,
                                        logDate = currentDateOnly(),
                                        status = status,
                                        takenAt = if (status == DoseStatus.TAKEN) timestamp else null,
                                        imageUri = null
                                    )
                                )
                                doseLogs = doseLogRepository.getAllDoseLogs()
                                doseLoggingMessage = when (status) {
                                    DoseStatus.SKIPPED -> "${doseItem.medicationName} marked as skipped."
                                    DoseStatus.SNOOZED -> "${doseItem.medicationName} marked as snoozed."
                                    DoseStatus.MISSED -> "${doseItem.medicationName} marked as missed."
                                    DoseStatus.TAKEN -> "${doseItem.medicationName} marked as taken."
                                }
                            }
                        },
                        doseLoggingMessage = doseLoggingMessage,
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
                                            val importResult = importQrPayload(
                                                rawValue = rawValue,
                                                medicationRepository = medicationRepository,
                                                doseScheduleRepository = doseScheduleRepository
                                            )
                                            medications = medicationRepository.getAllMedications()
                                            schedules = doseScheduleRepository.getAllDoseSchedules()
                                            if (importResult.insertedMedicationIds.size == 1) {
                                                val importedMedication = medications.firstOrNull {
                                                    it.id == importResult.insertedMedicationIds.first()
                                                }
                                                selectedMedication = importedMedication
                                                medicationReferenceImageUri = importedMedication?.referenceImageUri
                                                qrImportMessage =
                                                    "QR import complete. Review the medication, take its reference photo, and keep the pharmacy details locked unless you truly need to change them."
                                                currentScreen = AppScreen.MEDICATION_FORM
                                            } else {
                                                qrImportMessage =
                                                    "QR import complete. ${importResult.insertedMedicationIds.size} medication record(s) and ${importResult.autoScheduledCount} reminder time(s) were loaded."
                                                currentScreen = AppScreen.MEDICATION_LIST
                                            }
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
                        },
                        caregiverReportSummary = caregiverReportSummary,
                        caregiverReportQr = caregiverReportQr,
                        scannedCaregiverReportSummary = scannedCaregiverReportSummary,
                        scannedCaregiverReportMessage = scannedCaregiverReportMessage,
                        onScanCaregiverReportQr = {
                            val options = GmsBarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build()
                            val scanner = GmsBarcodeScanning.getClient(this@MainActivity, options)

                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val rawValue = barcode.rawValue
                                    if (rawValue.isNullOrBlank()) {
                                        scannedCaregiverReportSummary = null
                                        scannedCaregiverReportMessage =
                                            "The caregiver QR had no readable content."
                                        return@addOnSuccessListener
                                    }

                                    runCatching {
                                        CaregiverReportQrBuilder.parseQrPayload(rawValue)
                                    }.onSuccess { importedSummary ->
                                        scannedCaregiverReportSummary = importedSummary
                                        scannedCaregiverReportMessage = ""
                                    }.onFailure { error ->
                                        scannedCaregiverReportSummary = null
                                        scannedCaregiverReportMessage =
                                            error.message ?: "This QR is not a readable MediRemind caregiver report."
                                    }
                                }
                                .addOnCanceledListener {
                                    scannedCaregiverReportMessage = "Caregiver QR scan was canceled."
                                }
                                .addOnFailureListener { error ->
                                    scannedCaregiverReportSummary = null
                                    scannedCaregiverReportMessage =
                                        error.message ?: "Caregiver QR scan failed. Try again."
                                }
                        },
                        onBackFromCaregiverQr = {
                            currentScreen = AppScreen.HOME
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
    selectedSchedule: DoseSchedule?,
    medicationReferenceImageUri: String?,
    pendingDoseVerification: PendingDoseVerification?,
    onStartMedicationFlow: () -> Unit,
    onStartScheduleFlow: () -> Unit,
    onStartDoseLoggingFlow: () -> Unit,
    onStartQrImportFlow: () -> Unit,
    onStartPatientReportFlow: () -> Unit,
    onStartCaregiverScanFlow: () -> Unit,
    onOpenMedicationForm: () -> Unit,
    onOpenMedicationEditor: (Medication) -> Unit,
    onOpenScheduleForm: () -> Unit,
    onOpenScheduleEditor: (DoseSchedule) -> Unit,
    onBackHome: () -> Unit,
    onCancelMedicationForm: () -> Unit,
    onTakeMedicationReferencePhoto: () -> Unit,
    onSaveMedication: (Medication) -> Unit,
    onDeleteMedication: (Medication) -> Unit,
    onCancelScheduleForm: () -> Unit,
    onSaveSchedules: (List<DoseSchedule>) -> Unit,
    onBackFromDoseLogging: () -> Unit,
    onTakeDosePhoto: (DoseLoggingItem) -> Unit,
    onConfirmTaken: () -> Unit,
    onRetakeTakenPhoto: () -> Unit,
    onLogDose: (DoseLoggingItem, DoseStatus) -> Unit,
    doseLoggingMessage: String,
    qrImportMessage: String,
    qrRawPreview: String,
    onBackFromQrImport: () -> Unit,
    onScanMedicationQr: () -> Unit,
    caregiverReportSummary: CaregiverReportSummary?,
    caregiverReportQr: ImageBitmap?,
    onBackFromCaregiverQr: () -> Unit
) {
    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                modifier = modifier,
                onStartMedicationFlow = onStartMedicationFlow,
                onStartScheduleFlow = onStartScheduleFlow,
                onStartDoseLoggingFlow = onStartDoseLoggingFlow,
                onStartQrImportFlow = onStartQrImportFlow,
                onStartPatientReportFlow = onStartPatientReportFlow,
                onStartCaregiverScanFlow = onStartCaregiverScanFlow
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
                referenceImageUri = medicationReferenceImageUri,
                onTakeReferencePhoto = onTakeMedicationReferencePhoto,
                onSaveMedication = onSaveMedication,
                onDeleteMedication = onDeleteMedication,
                onCancel = onCancelMedicationForm
            )
        }

        AppScreen.SCHEDULE_LIST -> {
            ScheduleListScreen(
                modifier = modifier,
                scheduleGroups = buildScheduleDisplayGroups(
                    schedules = schedules,
                    medications = medications
                ),
                onAddScheduleClick = onOpenScheduleForm,
                onScheduleClick = onOpenScheduleEditor,
                onBackClick = onBackHome
            )
        }

        AppScreen.SCHEDULE_FORM -> {
            DoseScheduleFormScreen(
                modifier = modifier,
                medications = medications,
                existingSchedule = selectedSchedule,
                onSaveSchedules = onSaveSchedules,
                onCancel = onCancelScheduleForm
            )
        }

        AppScreen.DOSE_LOGGING -> {
            val todayDate = currentDateOnly()
            val activeSchedulesForToday = schedules
                .filter { schedule ->
                    isScheduleActiveOnDate(
                        schedule = schedule,
                        dateValue = todayDate
                    )
                }
                .sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }
            val todayLogs = doseLogs.filter { it.logDate == todayDate }
            val latestTodayLogBySchedule = todayLogs
                .groupBy { it.doseScheduleId }
                .mapValues { (_, logs) -> logs.maxByOrNull { it.id } }
            val remainingSchedules = activeSchedulesForToday.filter { schedule ->
                val latestLog = latestTodayLogBySchedule[schedule.id]
                latestLog == null || latestLog.status == DoseStatus.SNOOZED
            }
            val visibleRemainingSchedules = remainingSchedules.filter { schedule ->
                shouldShowInRemainingToday(
                    schedule = schedule,
                    todayLog = latestTodayLogBySchedule[schedule.id]
                )
            }
            DoseLoggingScreen(
                modifier = modifier,
                dueDoses = visibleRemainingSchedules
                    .map { schedule ->
                    val latestLog = latestTodayLogBySchedule[schedule.id]
                    val availability = evaluateDoseAvailability(
                        schedule = schedule,
                        todayLog = latestLog
                    )
                    DoseLoggingItem(
                        doseScheduleId = schedule.id,
                        medicationId = schedule.medicationId,
                        medicationName = medications.firstOrNull { it.id == schedule.medicationId }?.name
                            ?: "Unknown Medication",
                        scheduledTime = schedule.time,
                        frequencyLabel = schedule.frequency.name.lowercase().replace('_', ' '),
                        isActionAllowed = availability.isActionAllowed,
                        availabilityMessage = availability.message,
                        todayStatusLabel = latestLog?.status?.name
                            ?.lowercase()
                            ?.replaceFirstChar { it.uppercase() }
                    )
                },
                todaySummary = buildTodayDoseSummary(
                    dateLabel = formatTodaySummaryDate(todayDate),
                    schedules = activeSchedulesForToday,
                    visibleRemainingSchedules = visibleRemainingSchedules,
                    medications = medications,
                    latestTodayLogBySchedule = latestTodayLogBySchedule
                ),
                todayLoggedDoses = todayLogs
                    .sortedWith(
                        compareByDescending<DoseLog> { parseTimeToMinutes(it.scheduledTime) ?: Int.MIN_VALUE }
                            .thenByDescending { it.id }
                    )
                    .map { log ->
                        DoseLogDisplayItem(
                            medicationName = medications.firstOrNull { it.id == log.medicationId }?.name
                                ?: "Unknown Medication",
                            scheduledTime = log.scheduledTime,
                            logDate = log.logDate,
                            status = log.status,
                            takenAt = log.takenAt,
                            hasVerificationPhoto = !log.imageUri.isNullOrBlank(),
                            imageUri = log.imageUri
                        )
                    },
                recentLogs = doseLogs.map { log ->
                    DoseLogDisplayItem(
                        medicationName = medications.firstOrNull { it.id == log.medicationId }?.name
                            ?: "Unknown Medication",
                        scheduledTime = log.scheduledTime,
                        logDate = log.logDate,
                        status = log.status,
                        takenAt = log.takenAt,
                        hasVerificationPhoto = !log.imageUri.isNullOrBlank(),
                        imageUri = log.imageUri
                    )
                },
                pendingDoseVerification = pendingDoseVerification?.let { verification ->
                    PendingDoseVerificationDisplay(
                        medicationName = verification.doseItem.medicationName,
                        scheduledTime = verification.doseItem.scheduledTime,
                        referenceImageUri = verification.referenceImageUri,
                        capturedImageUri = verification.capturedImageUri,
                        isLikelyMatch = verification.isLikelyMatch,
                        matchMessage = verification.matchMessage,
                        similarityScore = verification.similarityScore
                    )
                },
                statusMessage = doseLoggingMessage,
                onConfirmTaken = onConfirmTaken,
                onRetakeTakenPhoto = onRetakeTakenPhoto,
                onTakeDosePhoto = onTakeDosePhoto,
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

        AppScreen.PATIENT_REPORT -> {
            PatientReportScreen(
                modifier = modifier,
                patientName = caregiverReportSummary?.patientName ?: "Patient Not Set",
                caregiverName = caregiverReportSummary?.caregiverName,
                reportDate = caregiverReportSummary?.reportDate ?: currentDateOnly(),
                adherenceRate = caregiverReportSummary?.adherenceRate ?: 0,
                totalTaken = caregiverReportSummary?.totalTaken ?: 0,
                totalSkipped = caregiverReportSummary?.totalSkipped ?: 0,
                totalSnoozed = caregiverReportSummary?.totalSnoozed ?: 0,
                totalMissed = caregiverReportSummary?.totalMissed ?: 0,
                totalLogged = caregiverReportSummary?.totalLogged ?: 0,
                medicationSummaries = caregiverReportSummary?.medicationSummaries ?: emptyList(),
                qrBitmap = caregiverReportQr,
                onBackClick = onBackFromCaregiverQr
            )
        }

        AppScreen.CAREGIVER_SCAN -> {
            CaregiverScanScreen(
                modifier = modifier,
                scannedReportSummary = scannedCaregiverReportSummary,
                scannedReportMessage = scannedCaregiverReportMessage,
                onScanReportQrClick = onScanCaregiverReportQr,
                onBackClick = onBackFromCaregiverQr
            )
        }
    }
}

private fun currentTimestamp(): String {
    return SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
}

private fun currentDateOnly(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

private fun evaluateDoseAvailability(
    schedule: DoseSchedule,
    todayLog: DoseLog?
): DoseAvailability {
    if (todayLog != null) {
        if (todayLog.status == DoseStatus.SNOOZED) {
            return DoseAvailability(
                isActionAllowed = true,
                message = "Available now"
            )
        }
        return DoseAvailability(
            isActionAllowed = false,
            message = "Already logged today"
        )
    }

    if (schedule.frequency == com.example.mediremind.data.model.DoseFrequency.AS_NEEDED) {
        return DoseAvailability(
            isActionAllowed = true,
            message = "Available now"
        )
    }

    val scheduleMinutes = parseTimeToMinutes(schedule.time)
        ?: return DoseAvailability(
            isActionAllowed = false,
            message = "Time unavailable"
        )
    val currentMinutes = Calendar.getInstance().let {
        it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
    }
    val minutesDifference = currentMinutes - scheduleMinutes
    val earlyWindowMinutes = 0
    val lateWindowMinutes = 60

    return when {
        minutesDifference < -earlyWindowMinutes -> {
            DoseAvailability(
                isActionAllowed = false,
                message = "Available at ${schedule.time}"
            )
        }
        minutesDifference > lateWindowMinutes -> {
            DoseAvailability(
                isActionAllowed = false,
                message = "Missed today"
            )
        }
        else -> {
            DoseAvailability(
                isActionAllowed = true,
                message = "Available now"
            )
        }
    }
}

private fun shouldShowInRemainingToday(
    schedule: DoseSchedule,
    todayLog: DoseLog?
): Boolean {
    if (todayLog != null && todayLog.status != DoseStatus.SNOOZED) {
        return false
    }

    val availability = evaluateDoseAvailability(
        schedule = schedule,
        todayLog = todayLog
    )

    return availability.message != "Missed today"
}

private fun buildTodayDoseSummary(
    dateLabel: String,
    schedules: List<DoseSchedule>,
    visibleRemainingSchedules: List<DoseSchedule>,
    medications: List<Medication>,
    latestTodayLogBySchedule: Map<Long, DoseLog?>
): TodayDoseSummaryDisplay {
    val nextDose = visibleRemainingSchedules
        .sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }
        .firstOrNull()

    val latestTodayLogs = latestTodayLogBySchedule.values.filterNotNull()
    val takenCount = latestTodayLogs.count { it.status == DoseStatus.TAKEN }
    val skippedCount = latestTodayLogs.count { it.status == DoseStatus.SKIPPED }
    val snoozedCount = latestTodayLogs.count { it.status == DoseStatus.SNOOZED }
    val missedCount = latestTodayLogs.count { it.status == DoseStatus.MISSED }
    val completedCount = latestTodayLogs.count { it.status != DoseStatus.SNOOZED }

    val nextDoseLabel = nextDose?.let { schedule ->
        val medicationName = medications.firstOrNull { it.id == schedule.medicationId }?.name
            ?: "Medication"
        "$medicationName at ${schedule.time}"
    }

    val summaryMessage = if (visibleRemainingSchedules.isEmpty()) {
        if (schedules.isEmpty()) {
            "No schedules saved for today yet."
        } else if (missedCount > 0) {
            "Today's dose window is closed. Review the missed doses below."
        } else {
            "All today's doses are done."
        }
    } else {
        "You still have ${visibleRemainingSchedules.size} dose(s) left today."
    }

    return TodayDoseSummaryDisplay(
        dateLabel = dateLabel,
        totalScheduled = schedules.size,
        completedCount = completedCount,
        remainingCount = visibleRemainingSchedules.size,
        takenCount = takenCount,
        skippedCount = skippedCount,
        snoozedCount = snoozedCount,
        missedCount = missedCount,
        nextDoseLabel = nextDoseLabel,
        summaryMessage = summaryMessage
    )
}

private fun buildAutoMissedDoseLogs(
    schedules: List<DoseSchedule>,
    existingLogs: List<DoseLog>,
    todayDate: String
): List<DoseLog> {
    val latestLogByScheduleAndDate = existingLogs
        .groupBy { "${it.doseScheduleId}|${it.logDate}" }
        .mapValues { (_, logs) -> logs.maxByOrNull { it.id } }
    val currentMinutes = currentMinutesOfDay()

    return schedules
        .flatMap { schedule ->
            buildRelevantScheduleDates(
                schedule = schedule,
                todayDate = todayDate
            ).mapNotNull { targetDate ->
                val latestLog = latestLogByScheduleAndDate["${schedule.id}|$targetDate"]
                val shouldAutoMiss = shouldAutoMarkMissed(
                    schedule = schedule,
                    latestLog = latestLog,
                    targetDate = targetDate,
                    todayDate = todayDate,
                    currentMinutes = currentMinutes
                )
                if (!shouldAutoMiss) {
                    null
                } else {
                    DoseLog(
                        doseScheduleId = schedule.id,
                        medicationId = schedule.medicationId,
                        scheduledTime = schedule.time,
                        logDate = targetDate,
                        status = DoseStatus.MISSED,
                        takenAt = null,
                        imageUri = null
                    )
                }
            }
        }
}

private fun buildRelevantScheduleDates(
    schedule: DoseSchedule,
    todayDate: String
): List<String> {
    if (schedule.frequency == com.example.mediremind.data.model.DoseFrequency.AS_NEEDED) {
        return emptyList()
    }

    val todayCalendar = parseDateOnly(todayDate) ?: return emptyList()
    val startCalendar = parseDateOnly(schedule.startDate) ?: todayCalendar
    val endCalendar = parseDateOnly(schedule.endDate)?.let { end ->
        if (end.after(todayCalendar)) todayCalendar else end
    } ?: todayCalendar

    if (startCalendar.after(endCalendar)) {
        return emptyList()
    }

    val dates = mutableListOf<String>()
    val cursor = startCalendar.clone() as Calendar

    while (!cursor.after(endCalendar) && dates.size < 366) {
        val dateValue = formatDateOnly(cursor)
        if (isScheduleActiveOnDate(schedule, dateValue)) {
            dates.add(dateValue)
        }
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }

    return dates
}

private fun shouldAutoMarkMissed(
    schedule: DoseSchedule,
    latestLog: DoseLog?,
    targetDate: String,
    todayDate: String,
    currentMinutes: Int
): Boolean {
    if (schedule.frequency == com.example.mediremind.data.model.DoseFrequency.AS_NEEDED) {
        return false
    }
    if (latestLog != null && latestLog.status != DoseStatus.SNOOZED) {
        return false
    }

    val scheduleMinutes = parseTimeToMinutes(schedule.time) ?: return false
    val currentMinutes = Calendar.getInstance().let {
        it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
    }
    val graceWindowMinutes = 60

    return currentMinutes > scheduleMinutes + graceWindowMinutes
}

private fun formatTodaySummaryDate(value: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(value) ?: return value
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        value
    }
}

private fun parseTimeToMinutes(value: String): Int? {
    return try {
        val parser = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = parser.parse(value) ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    } catch (_: Exception) {
        null
    }
}

private fun isScheduleActiveOnDate(
    schedule: DoseSchedule,
    dateValue: String
): Boolean {
    val currentDate = parseDateOnly(dateValue) ?: return true
    val startDate = parseDateOnly(schedule.startDate)
    val endDate = parseDateOnly(schedule.endDate)

    if (startDate != null && currentDate.before(startDate)) {
        return false
    }
    if (endDate != null && currentDate.after(endDate)) {
        return false
    }

    if (schedule.frequency == com.example.mediremind.data.model.DoseFrequency.WEEKLY) {
        val anchorDate = startDate ?: return true
        return currentDate.get(Calendar.DAY_OF_WEEK) == anchorDate.get(Calendar.DAY_OF_WEEK)
    }

    return true
}

private fun parseDateOnly(value: String): Calendar? {
    if (value.isBlank()) return null

    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value) ?: return null
        Calendar.getInstance().apply { time = date }
    } catch (_: Exception) {
        null
    }
}

private fun formatSchedulePeriod(
    startDate: String,
    endDate: String
): String {
    val formattedStartDate = formatDateShort(startDate)
    val formattedEndDate = formatDateShort(endDate)

    return when {
        formattedStartDate == null && formattedEndDate == null -> "Always active"
        formattedStartDate != null && formattedEndDate != null -> "$formattedStartDate - $formattedEndDate"
        formattedStartDate != null -> "From $formattedStartDate"
        else -> "Until $formattedEndDate"
    }
}

private fun buildScheduleDisplayGroups(
    schedules: List<DoseSchedule>,
    medications: List<Medication>
): List<ScheduleDisplayGroup> {
    return schedules
        .groupBy { schedule ->
            Triple(
                schedule.medicationId,
                schedule.frequency,
                "${schedule.startDate}|${schedule.endDate}"
            )
        }
        .map { (_, groupedSchedules) ->
            val firstSchedule = groupedSchedules.first()
            ScheduleDisplayGroup(
                medicationName = medications.firstOrNull { it.id == firstSchedule.medicationId }?.name
                    ?: "Unknown Medication",
                frequency = firstSchedule.frequency,
                periodLabel = formatSchedulePeriod(
                    startDate = firstSchedule.startDate,
                    endDate = firstSchedule.endDate
                ),
                timeEntries = groupedSchedules
                    .sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }
                    .map { schedule ->
                        ScheduleTimeDisplayItem(
                            schedule = schedule,
                            reminderTime = schedule.time
                        )
                    }
            )
        }
        .sortedWith(
            compareBy<ScheduleDisplayGroup> { it.medicationName }
                .thenBy { parseTimeToMinutes(it.timeEntries.firstOrNull()?.reminderTime.orEmpty()) ?: Int.MAX_VALUE }
        )
}

private fun formatDateShort(value: String): String? {
    val calendar = parseDateOnly(value) ?: return null
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun plusDays(value: String, days: Int): String {
    val calendar = parseDateOnly(value) ?: Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}

private fun currentMinuteRefreshKey(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
}

private fun compareMedicationPhotos(
    context: Context,
    referenceImageUri: String,
    capturedImageUri: String
): PhotoComparisonResult {
    val referenceBitmap = loadBitmapFromUri(context, referenceImageUri)
    val capturedBitmap = loadBitmapFromUri(context, capturedImageUri)

    if (referenceBitmap == null || capturedBitmap == null) {
        return PhotoComparisonResult(
            isLikelyMatch = false,
            similarityScore = 0,
            message = "Could not compare the medicine photos. Retake the photo and try again."
        )
    }

    val referenceHash = buildAverageHash(referenceBitmap)
    val capturedHash = buildAverageHash(capturedBitmap)
    val distance = hammingDistance(referenceHash, capturedHash)
    val similarityScore = ((64 - distance) * 100 / 64).coerceIn(0, 100)
    val isLikelyMatch = similarityScore >= 58

    return PhotoComparisonResult(
        isLikelyMatch = isLikelyMatch,
        similarityScore = similarityScore,
        message = if (isLikelyMatch) {
            "Live photo looks close enough to the saved medicine reference."
        } else {
            "Live photo does not look close enough to the saved medicine. Please retake the actual medicine photo."
        }
    )
}

private fun loadBitmapFromUri(
    context: Context,
    uriString: String
): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(Uri.parse(uriString))?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }.getOrNull()
}

private fun buildAverageHash(bitmap: Bitmap): Long {
    val croppedBitmap = centerCropSquare(bitmap)
    val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 8, 8, true)
    val pixels = IntArray(64)
    resizedBitmap.getPixels(pixels, 0, 8, 0, 0, 8, 8)
    val grayscaleValues = pixels.map { pixel ->
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        (red + green + blue) / 3
    }
    val average = grayscaleValues.average()

    var hash = 0L
    grayscaleValues.forEachIndexed { index, value ->
        if (value >= average) {
            hash = hash or (1L shl index)
        }
    }

    return hash
}

private fun centerCropSquare(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val xOffset = (bitmap.width - size) / 2
    val yOffset = (bitmap.height - size) / 2
    return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
}

private fun hammingDistance(first: Long, second: Long): Int {
    return java.lang.Long.bitCount(first xor second)
}

private fun createAppImageUri(
    context: Context,
    folderName: String,
    filePrefix: String
): Uri {
    val imageDirectory = File(context.cacheDir, folderName).apply {
        mkdirs()
    }
    val imageFile = File(
        imageDirectory,
        "${filePrefix}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private suspend fun importQrPayload(
    rawValue: String,
    medicationRepository: MedicationRepository,
    doseScheduleRepository: DoseScheduleRepository
): QrImportResult {
    val payload = QrImportParser.parse(rawValue)
    val startDate = plusDays(currentDateOnly(), 1)
    val defaultEndDate = plusDays(startDate, 29)
    val insertedMedicationIds = mutableListOf<Long>()
    var autoScheduledCount = 0

    payload.medications.forEach { importedMedication ->
        val medicationId = medicationRepository.insertMedication(importedMedication.medication)
        insertedMedicationIds.add(medicationId)

        val schedulesToSave = importedMedication.times.map { time ->
            DoseSchedule(
                medicationId = medicationId,
                time = time,
                frequency = importedMedication.frequency,
                startDate = startDate,
                endDate = defaultEndDate
            )
        }

        doseScheduleRepository.insertDoseSchedules(schedulesToSave)
        autoScheduledCount += schedulesToSave.size
    }

    return QrImportResult(
        insertedMedicationIds = insertedMedicationIds,
        autoScheduledCount = autoScheduledCount
    )
}
