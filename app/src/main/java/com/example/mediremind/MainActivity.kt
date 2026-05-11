package com.example.mediremind

import android.content.Context
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
import com.example.mediremind.domain.MedicationPhotoMatcher
import com.example.mediremind.ui.screen.home.HomeScreen
import com.example.mediremind.ui.screen.medication.MedicationFormScreen
import com.example.mediremind.ui.screen.medication.MedicationListScreen
import com.example.mediremind.ui.screen.medication.QrImportScreen
import com.example.mediremind.ui.screen.profile.PatientProfileScreen
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
import java.text.SimpleDateFormat
import java.util.Calendar
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
    PROFILE,
    PATIENT_REPORT,
    CAREGIVER_SCAN
}

private data class PendingDoseVerification(
    val doseItem: DoseLoggingItem,
    val referenceImageUri: String,
    val capturedImageUri: String,
    val isLikelyMatch: Boolean,
    val isManualOverrideAllowed: Boolean,
    val matchMessage: String,
    val similarityScore: Int,
    val debugDetail: String
)

private data class DoseAvailability(
    val isActionAllowed: Boolean,
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
            var allProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
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
            var activePatientId by remember { mutableStateOf(UserProfileRepository.LEGACY_PATIENT_ID) }
            var timeRefreshKey by remember { mutableStateOf(currentMinuteRefreshKey()) }

            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                cleanupExpiredVerificationPhotos(
                    context = applicationContext,
                    doseLogRepository = doseLogRepository
                )
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
                    coroutineScope.launch {
                        val matchResult = MedicationPhotoMatcher.compare(
                            context = applicationContext,
                            referenceUri = referenceImageUri,
                            capturedUri = capturedUri.toString()
                        )
                        pendingDoseVerification = PendingDoseVerification(
                            doseItem = capturedDose,
                            referenceImageUri = referenceImageUri,
                            capturedImageUri = capturedUri.toString(),
                            isLikelyMatch = matchResult.zone == MedicationPhotoMatcher.MatchZone.MATCH,
                            isManualOverrideAllowed = matchResult.zone == MedicationPhotoMatcher.MatchZone.WARNING,
                            matchMessage = matchResult.message,
                            similarityScore = matchResult.score,
                            debugDetail = matchResult.debugDetail
                        )
                        doseLoggingMessage = if (matchResult.zone == MedicationPhotoMatcher.MatchZone.MATCH) {
                            ""
                        } else {
                            matchResult.message
                        }
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
                activePatientId = userProfileRepository.getActivePatientId()
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.MEDICATION_LIST) {
                    medications = medicationRepository.getMedicationsForPatient(activePatientId)
                }
                if (
                    currentScreen == AppScreen.HOME ||
                    currentScreen == AppScreen.SCHEDULE_LIST ||
                    currentScreen == AppScreen.SCHEDULE_FORM ||
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PROFILE ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    medications = medicationRepository.getMedicationsForPatient(activePatientId)
                }
                if (currentScreen == AppScreen.HOME || currentScreen == AppScreen.PROFILE || currentScreen == AppScreen.PATIENT_REPORT) {
                    allProfiles = userProfileRepository.getAllProfiles()
                    userProfile = userProfileRepository.getActiveProfile()
                }
                if (
                    currentScreen == AppScreen.HOME ||
                    currentScreen == AppScreen.SCHEDULE_LIST ||
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    schedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
                }
                if (
                    currentScreen == AppScreen.HOME ||
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
                }
                if (
                    currentScreen == AppScreen.DOSE_LOGGING ||
                    currentScreen == AppScreen.PATIENT_REPORT
                ) {
                    val loadedSchedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
                    val loadedDoseLogs = doseLogRepository.getLogsForPatient(activePatientId)
                    val autoMissedLogs = buildAutoMissedDoseLogs(
                        schedules = loadedSchedules,
                        existingLogs = loadedDoseLogs,
                        todayDate = currentDateOnly()
                    )
                    if (autoMissedLogs.isNotEmpty()) {
                        autoMissedLogs.forEach { doseLogRepository.insertDoseLog(it) }
                        doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
                    }
                }
                if (currentScreen == AppScreen.PATIENT_REPORT) {
                    val loadedProfile = userProfileRepository.getActiveProfile()
                    val loadedMedications = medicationRepository.getMedicationsForPatient(activePatientId)
                    val loadedSchedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
                    val loadedDoseLogs = doseLogRepository.getLogsForPatient(activePatientId)

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
                        CaregiverReportQrBuilder.generateQrBitmap(summary.qrPayload)?.asImageBitmap()
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
                        onStartProfileFlow = {
                            currentScreen = AppScreen.PROFILE
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
                                val patientId = activePatientId
                                val medicationToSave = medication.copy(patientId = patientId)
                                val savedMedicationId = if (medication.id == 0L) {
                                    medicationRepository.insertMedication(medicationToSave)
                                } else {
                                    medicationRepository.updateMedication(medicationToSave)
                                    medication.id
                                }

                                if (savedMedicationId != 0L) {
                                    syncMedicationSchedulesFromMedicationDetails(
                                        medication = medicationToSave.copy(id = savedMedicationId),
                                        doseScheduleRepository = doseScheduleRepository
                                    )
                                }
                                medications = medicationRepository.getMedicationsForPatient(patientId)
                                schedules = doseScheduleRepository.getSchedulesForPatient(patientId)
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
                                medications = medicationRepository.getMedicationsForPatient(activePatientId)
                                schedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
                                doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
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
                                val schedulesToSave = newSchedules.map { schedule ->
                                    schedule.copy(patientId = activePatientId)
                                }
                                if (selectedSchedule != null && newSchedules.size == 1) {
                                    doseScheduleRepository.updateDoseSchedule(schedulesToSave.first())
                                } else {
                                    doseScheduleRepository.insertDoseSchedules(schedulesToSave)
                                }
                                schedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
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
                                val canConfirm =
                                    verification.isLikelyMatch || verification.isManualOverrideAllowed
                                if (!canConfirm) {
                                    doseLoggingMessage = verification.matchMessage
                                } else {
                                    coroutineScope.launch {
                                        val shouldDeductStock = shouldDeductStockForTakenDose(
                                            existingLogs = doseLogRepository.getLogsForPatient(activePatientId),
                                            doseScheduleId = verification.doseItem.doseScheduleId,
                                            logDate = currentDateOnly()
                                        )
                                        doseLogRepository.insertDoseLog(
                                            DoseLog(
                                                patientId = activePatientId,
                                                doseScheduleId = verification.doseItem.doseScheduleId,
                                                medicationId = verification.doseItem.medicationId,
                                                scheduledTime = verification.doseItem.scheduledTime,
                                                logDate = currentDateOnly(),
                                                status = DoseStatus.TAKEN,
                                                takenAt = currentTimestamp(),
                                                imageUri = verification.capturedImageUri
                                            )
                                        )
                                        if (shouldDeductStock) {
                                            decrementMedicationStock(
                                                medicationId = verification.doseItem.medicationId,
                                                patientId = activePatientId,
                                                medicationRepository = medicationRepository
                                            )
                                        }
                                        medications = medicationRepository.getMedicationsForPatient(activePatientId)
                                        doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
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
                                val logDate = currentDateOnly()
                                val shouldDeductStock = status == DoseStatus.TAKEN &&
                                    shouldDeductStockForTakenDose(
                                        existingLogs = doseLogRepository.getLogsForPatient(activePatientId),
                                        doseScheduleId = doseItem.doseScheduleId,
                                        logDate = logDate
                                    )
                                doseLogRepository.insertDoseLog(
                                    DoseLog(
                                        patientId = activePatientId,
                                        doseScheduleId = doseItem.doseScheduleId,
                                        medicationId = doseItem.medicationId,
                                        scheduledTime = doseItem.scheduledTime,
                                        logDate = logDate,
                                        status = status,
                                        takenAt = if (status == DoseStatus.TAKEN) timestamp else null,
                                        imageUri = null
                                    )
                                )
                                if (shouldDeductStock) {
                                    decrementMedicationStock(
                                        medicationId = doseItem.medicationId,
                                        patientId = activePatientId,
                                        medicationRepository = medicationRepository
                                    )
                                    medications = medicationRepository.getMedicationsForPatient(activePatientId)
                                }
                                doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
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
                        onSaveUserProfile = { profile ->
                            coroutineScope.launch {
                                userProfileRepository.saveUserProfile(profile)
                                activePatientId = userProfileRepository.getActivePatientId()
                                allProfiles = userProfileRepository.getAllProfiles()
                                userProfile = userProfileRepository.getActiveProfile()
                                medications = medicationRepository.getMedicationsForPatient(activePatientId)
                                schedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
                                doseLogs = doseLogRepository.getLogsForPatient(activePatientId)
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onSwitchUserProfile = { profile ->
                            coroutineScope.launch {
                                userProfileRepository.setActivePatientId(profile.id)
                                activePatientId = profile.id
                                userProfile = profile
                                allProfiles = userProfileRepository.getAllProfiles()
                                medications = medicationRepository.getMedicationsForPatient(profile.id)
                                schedules = doseScheduleRepository.getSchedulesForPatient(profile.id)
                                doseLogs = doseLogRepository.getLogsForPatient(profile.id)
                            }
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
                                                patientId = activePatientId,
                                                medicationRepository = medicationRepository,
                                                doseScheduleRepository = doseScheduleRepository
                                            )
                                            medications = medicationRepository.getMedicationsForPatient(activePatientId)
                                            schedules = doseScheduleRepository.getSchedulesForPatient(activePatientId)
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
                        userProfile = userProfile,
                        allProfiles = allProfiles,
                        activePatientId = activePatientId,
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
    onStartProfileFlow: () -> Unit,
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
    onSaveUserProfile: (UserProfile) -> Unit,
    onSwitchUserProfile: (UserProfile) -> Unit,
    onScanMedicationQr: () -> Unit,
    caregiverReportSummary: CaregiverReportSummary?,
    caregiverReportQr: ImageBitmap?,
    scannedCaregiverReportSummary: CaregiverReportSummary?,
    scannedCaregiverReportMessage: String,
    onScanCaregiverReportQr: () -> Unit,
    userProfile: UserProfile?,
    allProfiles: List<UserProfile>,
    activePatientId: Long,
    onBackFromCaregiverQr: () -> Unit
) {
    when (currentScreen) {
        AppScreen.HOME -> {
            val todayDate = currentDateOnly()
            val dueTodayCount = schedules.count { schedule ->
                isScheduleActiveOnDate(schedule, todayDate)
            }
            val loggedTodayCount = doseLogs.count { log ->
                log.logDate == todayDate && log.status != DoseStatus.MISSED
            }
            val nextStepLabel = when {
                userProfile == null -> "Save the patient profile first so reports and caregiver sharing use the right patient name."
                medications.isEmpty() -> "Add the patient's medicines next so MediRemind can start tracking treatment."
                schedules.isEmpty() -> "Set the dose schedule next so the app knows when each medicine should be taken."
                dueTodayCount > loggedTodayCount -> "Open Dose Logging to review the doses that still need action today."
                else -> "Open Patient Report or Caregiver Scan to review the latest adherence summary."
            }
            HomeScreen(
                modifier = modifier,
                patientName = userProfile?.fullName,
                medicationCount = medications.size,
                scheduleCount = schedules.size,
                dueTodayCount = dueTodayCount,
                loggedTodayCount = loggedTodayCount,
                nextStepLabel = nextStepLabel,
                onStartMedicationFlow = onStartMedicationFlow,
                onStartScheduleFlow = onStartScheduleFlow,
                onStartDoseLoggingFlow = onStartDoseLoggingFlow,
                onStartQrImportFlow = onStartQrImportFlow,
                onStartProfileFlow = onStartProfileFlow,
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
                        isManualOverrideAllowed = verification.isManualOverrideAllowed,
                        matchMessage = verification.matchMessage,
                        similarityScore = verification.similarityScore,
                        debugDetail = verification.debugDetail
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
                activePatientName = userProfile?.fullName?.takeIf { it.isNotBlank() }
                    ?: "Patient profile not set",
                importMessage = qrImportMessage,
                rawScanPreview = qrRawPreview,
                onScanQrClick = onScanMedicationQr,
                onBackClick = onBackFromQrImport
            )
        }

        AppScreen.PROFILE -> {
            PatientProfileScreen(
                modifier = modifier,
                existingProfile = userProfile,
                profiles = allProfiles,
                activePatientId = activePatientId,
                onSaveProfile = onSaveUserProfile,
                onSwitchProfile = onSwitchUserProfile,
                onBackClick = onBackFromCaregiverQr
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
    val earlyWindowMinutes = 30
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
                        patientId = schedule.patientId,
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

    if (targetDate != todayDate) {
        return true
    }

    val scheduleMinutes = parseTimeToMinutes(schedule.time) ?: return false
    val graceWindowMinutes = 60

    return currentMinutes > scheduleMinutes + graceWindowMinutes
}

private fun currentMinutesOfDay(): Int {
    return Calendar.getInstance().let {
        it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
    }
}

private fun formatDateOnly(calendar: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
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
    val todayDate = currentDateOnly()

    return schedules
        .filter { schedule ->
            schedule.endDate.isBlank() || isScheduleActiveOnOrAfterToday(
                schedule = schedule,
                todayDate = todayDate
            )
        }
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

private fun isScheduleActiveOnOrAfterToday(
    schedule: DoseSchedule,
    todayDate: String
): Boolean {
    val todayCalendar = parseDateOnly(todayDate) ?: return true
    val endCalendar = parseDateOnly(schedule.endDate) ?: return true
    return !endCalendar.before(todayCalendar)
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

private fun formatDateShort(value: String): String? {
    val calendar = parseDateOnly(value) ?: return null
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private suspend fun syncMedicationSchedulesFromMedicationDetails(
    medication: Medication,
    doseScheduleRepository: DoseScheduleRepository
) {
    val inferredFrequency = inferDoseFrequencyFromDosageText(medication.dosage) ?: return
    val existingSchedules = doseScheduleRepository.getSchedulesForMedicationForPatient(
        medicationId = medication.id,
        patientId = medication.patientId
    )
    val todayDate = currentDateOnly()

    if (existingSchedules.isEmpty()) {
        val newSchedules = defaultReminderTimesForFrequency(inferredFrequency).map { time ->
            val startDate = startDateForNewReminderTime(
                time = time,
                todayDate = todayDate
            )
            DoseSchedule(
                patientId = medication.patientId,
                medicationId = medication.id,
                time = time,
                frequency = inferredFrequency,
                startDate = startDate,
                endDate = calculateEndDateFromMedication(
                    medication = medication,
                    frequency = inferredFrequency,
                    startDate = startDate
                )
            )
        }
        doseScheduleRepository.insertDoseSchedules(newSchedules)
        return
    }

    val activeSchedules = existingSchedules.filter { schedule ->
        schedule.endDate.isBlank() || isScheduleActiveOnOrAfterToday(
            schedule = schedule,
            todayDate = todayDate
        )
    }.sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }

    if (activeSchedules.isEmpty()) return

    val startDate = activeSchedules.minByOrNull { parseDateOnly(it.startDate)?.timeInMillis ?: Long.MAX_VALUE }
        ?.startDate
        ?.takeIf { it.isNotBlank() }
        ?: plusDays(todayDate, 1)
    val endDate = calculateEndDateFromMedication(
        medication = medication,
        frequency = inferredFrequency,
        startDate = startDate
    )
    val reminderTimes = resolveReminderTimesForFrequency(
        frequency = inferredFrequency,
        existingSchedules = activeSchedules
    )

    activeSchedules.take(reminderTimes.size).forEachIndexed { index, schedule ->
        doseScheduleRepository.updateDoseSchedule(
            schedule.copy(
                time = reminderTimes[index],
                frequency = inferredFrequency,
                startDate = startDate,
                endDate = endDate
            )
        )
    }

    if (activeSchedules.size < reminderTimes.size) {
        val newSchedules = reminderTimes.drop(activeSchedules.size).map { time ->
            DoseSchedule(
                patientId = medication.patientId,
                medicationId = medication.id,
                time = time,
                frequency = inferredFrequency,
                startDate = startDate,
                endDate = endDate
            )
        }
        doseScheduleRepository.insertDoseSchedules(newSchedules)
    }

    val obsoleteSchedules = activeSchedules.drop(reminderTimes.size)
    if (obsoleteSchedules.isNotEmpty()) {
        val archivedEndDate = plusDays(todayDate, -1)
        obsoleteSchedules.forEach { obsoleteSchedule ->
            doseScheduleRepository.updateDoseSchedule(
                obsoleteSchedule.copy(endDate = archivedEndDate)
            )
        }
    }
}

private fun inferDoseFrequencyFromDosageText(
    dosageText: String
): com.example.mediremind.data.model.DoseFrequency? {
    val normalized = dosageText.lowercase()
    return when {
        "three times daily" in normalized || "3 times daily" in normalized -> com.example.mediremind.data.model.DoseFrequency.THREE_TIMES_DAILY
        "twice daily" in normalized || "2 times daily" in normalized -> com.example.mediremind.data.model.DoseFrequency.TWICE_DAILY
        "once daily" in normalized || "daily" in normalized || "every morning" in normalized -> com.example.mediremind.data.model.DoseFrequency.ONCE_DAILY
        "weekly" in normalized || "once weekly" in normalized -> com.example.mediremind.data.model.DoseFrequency.WEEKLY
        "as needed" in normalized || "when needed" in normalized -> com.example.mediremind.data.model.DoseFrequency.AS_NEEDED
        else -> null
    }
}

private fun resolveReminderTimesForFrequency(
    frequency: com.example.mediremind.data.model.DoseFrequency,
    existingSchedules: List<DoseSchedule>
): List<String> {
    val existingTimes = existingSchedules
        .sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }
        .map { it.time }
    val desiredCount = countReminderSlotsForFrequency(frequency)
    val fallbackTimes = defaultReminderTimesForFrequency(frequency)

    return buildList {
        repeat(desiredCount) { index ->
            add(existingTimes.getOrNull(index) ?: fallbackTimes.getOrElse(index) { fallbackTimes.lastOrNull().orEmpty() })
        }
    }
}

private fun countReminderSlotsForFrequency(
    frequency: com.example.mediremind.data.model.DoseFrequency
): Int {
    return when (frequency) {
        com.example.mediremind.data.model.DoseFrequency.ONCE_DAILY,
        com.example.mediremind.data.model.DoseFrequency.WEEKLY,
        com.example.mediremind.data.model.DoseFrequency.AS_NEEDED -> 1
        com.example.mediremind.data.model.DoseFrequency.TWICE_DAILY -> 2
        com.example.mediremind.data.model.DoseFrequency.THREE_TIMES_DAILY -> 3
    }
}

private fun defaultReminderTimesForFrequency(
    frequency: com.example.mediremind.data.model.DoseFrequency
): List<String> {
    return when (frequency) {
        com.example.mediremind.data.model.DoseFrequency.ONCE_DAILY -> listOf("09:00 AM")
        com.example.mediremind.data.model.DoseFrequency.TWICE_DAILY -> listOf("09:00 AM", "09:00 PM")
        com.example.mediremind.data.model.DoseFrequency.THREE_TIMES_DAILY -> listOf("09:00 AM", "01:00 PM", "09:00 PM")
        com.example.mediremind.data.model.DoseFrequency.WEEKLY -> listOf("09:00 AM")
        com.example.mediremind.data.model.DoseFrequency.AS_NEEDED -> listOf("09:00 AM")
    }
}

private fun startDateForNewReminderTime(
    time: String,
    todayDate: String
): String {
    val scheduleMinutes = parseTimeToMinutes(time) ?: return todayDate
    val lateWindowMinutes = 60
    return if (currentMinutesOfDay() > scheduleMinutes + lateWindowMinutes) {
        plusDays(todayDate, 1)
    } else {
        todayDate
    }
}

private fun calculateEndDateFromMedication(
    medication: Medication,
    frequency: com.example.mediremind.data.model.DoseFrequency,
    startDate: String
): String {
    val remindersPerDay = countReminderSlotsForFrequency(frequency).coerceAtLeast(1)
    val estimatedDaysOfSupply = kotlin.math.ceil(
        medication.currentStockAmount / remindersPerDay.toDouble()
    ).toInt().coerceAtLeast(1)
    return plusDays(startDate, estimatedDaysOfSupply - 1)
}

private fun plusDays(value: String, days: Int): String {
    val calendar = parseDateOnly(value) ?: Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}

private fun currentMinuteRefreshKey(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
}

private fun createAppImageUri(
    context: Context,
    folderName: String,
    filePrefix: String
): Uri {
    val imageDirectory = File(context.filesDir, "medication_photos/$folderName").apply {
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

private suspend fun cleanupExpiredVerificationPhotos(
    context: Context,
    doseLogRepository: DoseLogRepository
) {
    val verificationDirectory = File(context.filesDir, "medication_photos/verification_images")
    if (!verificationDirectory.exists()) return

    val cutoffMillis = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
    val deletedFileNames = verificationDirectory
        .listFiles()
        .orEmpty()
        .filter { file -> file.isFile && file.lastModified() < cutoffMillis }
        .mapNotNull { file ->
            val fileName = file.name
            if (file.delete()) fileName else null
        }
        .toSet()

    val existingFileNames = verificationDirectory
        .listFiles()
        .orEmpty()
        .filter { file -> file.isFile }
        .map { file -> file.name }
        .toSet()

    doseLogRepository.getAllDoseLogs()
        .filter { log -> !log.imageUri.isNullOrBlank() }
        .forEach { log ->
            val imageFileName = log.imageUri?.let { imageUri ->
                Uri.parse(imageUri).lastPathSegment?.substringAfterLast('/')
            }
            val shouldClearPhotoReference = imageFileName != null &&
                imageFileName.startsWith("dose_") &&
                (imageFileName in deletedFileNames || imageFileName !in existingFileNames)

            if (shouldClearPhotoReference) {
                doseLogRepository.updateDoseLog(log.copy(imageUri = null))
            }
        }
}

private suspend fun decrementMedicationStock(
    medicationId: Long,
    patientId: Long,
    medicationRepository: MedicationRepository
) {
    val medication = medicationRepository.getMedicationByIdForPatient(
        id = medicationId,
        patientId = patientId
    ) ?: return
    if (medication.currentStockAmount <= 0.0) return

    medicationRepository.updateMedication(
        medication.copy(
            currentStockAmount = (medication.currentStockAmount - 1.0).coerceAtLeast(0.0)
        )
    )
}

private fun shouldDeductStockForTakenDose(
    existingLogs: List<DoseLog>,
    doseScheduleId: Long,
    logDate: String
): Boolean {
    return existingLogs.none { log ->
        log.doseScheduleId == doseScheduleId &&
            log.logDate == logDate &&
            log.status == DoseStatus.TAKEN
    }
}

private suspend fun importQrPayload(
    rawValue: String,
    patientId: Long,
    medicationRepository: MedicationRepository,
    doseScheduleRepository: DoseScheduleRepository
): QrImportResult {
    val payload = QrImportParser.parse(rawValue)
    val todayDate = currentDateOnly()
    val insertedMedicationIds = mutableListOf<Long>()
    var autoScheduledCount = 0
    val existingMedications = medicationRepository.getMedicationsForPatient(patientId)

    payload.medications.forEach { importedMedication ->
        val matchedMedication = existingMedications.firstOrNull { existingMedication ->
            existingMedication.name.trim().equals(importedMedication.medication.name.trim(), ignoreCase = true) &&
                existingMedication.form == importedMedication.medication.form
        }

        val medicationId = if (matchedMedication != null) {
            medicationRepository.updateMedication(
                importedMedication.medication.copy(
                    id = matchedMedication.id,
                    patientId = patientId,
                    referenceImageUri = matchedMedication.referenceImageUri
                        ?: importedMedication.medication.referenceImageUri,
                    isQrImported = true
                )
            )
            matchedMedication.id
        } else {
            medicationRepository.insertMedication(
                importedMedication.medication.copy(patientId = patientId)
            )
        }
        insertedMedicationIds.add(medicationId)

        val existingSchedules = doseScheduleRepository.getSchedulesForMedicationForPatient(
            medicationId = medicationId,
            patientId = patientId
        )
            .filter { schedule ->
                schedule.endDate.isBlank() || isScheduleActiveOnOrAfterToday(
                    schedule = schedule,
                    todayDate = currentDateOnly()
                )
            }
            .sortedBy { parseTimeToMinutes(it.time) ?: Int.MAX_VALUE }
        val desiredTimes = importedMedication.times
        val desiredCount = desiredTimes.size

        existingSchedules.take(desiredCount).forEachIndexed { index, schedule ->
            val startDate = startDateForNewReminderTime(
                time = desiredTimes[index],
                todayDate = todayDate
            )
            doseScheduleRepository.updateDoseSchedule(
                schedule.copy(
                    patientId = patientId,
                    time = desiredTimes[index],
                    frequency = importedMedication.frequency,
                    startDate = startDate,
                    endDate = plusDays(startDate, 29)
                )
            )
        }

        if (existingSchedules.size < desiredCount) {
            val schedulesToSave = desiredTimes.drop(existingSchedules.size).map { time ->
                val startDate = startDateForNewReminderTime(
                    time = time,
                    todayDate = todayDate
                )
                DoseSchedule(
                    patientId = patientId,
                    medicationId = medicationId,
                    time = time,
                    frequency = importedMedication.frequency,
                    startDate = startDate,
                    endDate = plusDays(startDate, 29)
                )
            }
            doseScheduleRepository.insertDoseSchedules(schedulesToSave)
        }

        existingSchedules.drop(desiredCount).forEach { obsoleteSchedule ->
            doseScheduleRepository.updateDoseSchedule(
                obsoleteSchedule.copy(endDate = plusDays(currentDateOnly(), -1))
            )
        }

        autoScheduledCount += desiredCount
    }

    return QrImportResult(
        insertedMedicationIds = insertedMedicationIds,
        autoScheduledCount = autoScheduledCount
    )
}
