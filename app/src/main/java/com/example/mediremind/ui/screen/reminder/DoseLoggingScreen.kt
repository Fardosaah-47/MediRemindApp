package com.example.mediremind.ui.screen.reminder

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.ui.components.InfoCard
import com.example.mediremind.ui.components.IconBadge
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.StatusChip
import com.example.mediremind.ui.components.SurfaceCard
import com.example.mediremind.ui.theme.AlertCoral
import com.example.mediremind.ui.theme.ClinicTeal
import com.example.mediremind.ui.theme.DangerLight
import com.example.mediremind.ui.theme.DangerRed
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediMint
import com.example.mediremind.ui.theme.MediMuted
import com.example.mediremind.ui.theme.MediPrimaryDark
import com.example.mediremind.ui.theme.MediPrimaryLight
import com.example.mediremind.ui.theme.MediSurfaceRaised
import com.example.mediremind.ui.theme.MediRemindTheme
import com.example.mediremind.ui.theme.SuccessGreen
import com.example.mediremind.ui.theme.SuccessLight
import com.example.mediremind.ui.theme.WarningLight
import com.example.mediremind.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.Comparator

data class DoseLoggingItem(
    val doseScheduleId: Long,
    val medicationId: Long,
    val medicationName: String,
    val scheduledTime: String,
    val frequencyLabel: String,
    val stockLabel: String = "",
    val isLowStock: Boolean = false,
    val isActionAllowed: Boolean = true,
    val availabilityMessage: String = "",
    val todayStatusLabel: String? = null
)

data class DoseLogDisplayItem(
    val medicationName: String,
    val scheduledTime: String,
    val logDate: String,
    val status: DoseStatus,
    val takenAt: String?,
    val hasVerificationPhoto: Boolean = false,
    val imageUri: String? = null
)

data class PendingDoseVerificationDisplay(
    val medicationName: String,
    val scheduledTime: String,
    val referenceImageUri: String,
    val capturedImageUri: String,
    val isLikelyMatch: Boolean,
    val isManualOverrideAllowed: Boolean = false,
    val matchMessage: String,
    val similarityScore: Int,
    val debugDetail: String = ""
)

data class TodayDoseSummaryDisplay(
    val dateLabel: String,
    val totalScheduled: Int,
    val completedCount: Int,
    val remainingCount: Int,
    val takenCount: Int,
    val skippedCount: Int,
    val snoozedCount: Int,
    val missedCount: Int,
    val nextDoseLabel: String?,
    val summaryMessage: String
)

@Composable
fun DoseLoggingScreen(
    modifier: Modifier = Modifier,
    dueDoses: List<DoseLoggingItem> = sampleDoseLoggingItems(),
    todayLoggedDoses: List<DoseLogDisplayItem> = sampleDoseLogDisplayItems().filter { it.logDate == "2026-04-18" },
    recentLogs: List<DoseLogDisplayItem> = sampleDoseLogDisplayItems(),
    todaySummary: TodayDoseSummaryDisplay? = null,
    pendingDoseVerification: PendingDoseVerificationDisplay? = null,
    statusMessage: String = "",
    onConfirmTaken: () -> Unit = {},
    onRetakeTakenPhoto: () -> Unit = {},
    onTakeDosePhoto: (DoseLoggingItem) -> Unit = {},
    onLogDose: (DoseLoggingItem, DoseStatus) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var selectedHistoryRange by rememberSaveable { mutableStateOf(LogHistoryRange.WEEK) }
    var expandedMedicationHistoryKey by rememberSaveable { mutableStateOf<String?>(null) }
    val filteredRecentLogs = remember(recentLogs, selectedHistoryRange) {
        filterDoseLogsForRange(recentLogs, selectedHistoryRange)
    }
    val loggedTodaySections = remember(todayLoggedDoses) {
        buildStatusSections(todayLoggedDoses)
    }
    val historyDateSections = remember(filteredRecentLogs) {
        buildGroupedDateSections(filteredRecentLogs)
    }
    val medicationHistorySummaries = remember(filteredRecentLogs, selectedHistoryRange) {
        if (selectedHistoryRange == LogHistoryRange.TODAY) {
            emptyList()
        } else {
            buildMedicationHistorySummaries(filteredRecentLogs)
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            MediRemindTopBar(
                title = "Dose Logging",
                onBackClick = onBackClick
            )
        },
        containerColor = MediCream
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            todaySummary?.let { summary ->
                item {
                    TodaySummaryCard(summary = summary)
                }
            }

            if (pendingDoseVerification != null) {
                item {
                    VerificationStatusCard(verification = pendingDoseVerification)
                }

                item {
                    VerificationCompareCard(
                        verification = pendingDoseVerification,
                        onConfirmTaken = onConfirmTaken,
                        onRetakeTakenPhoto = onRetakeTakenPhoto
                    )
                }
            } else  if (statusMessage.isNotBlank()) {
                item {
                    InfoCard(title = "Dose Update") {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                SectionLabel(text = "Remaining today")
            }

            if (dueDoses.isEmpty()) {
                item {
                    EmptyDoseStateCard(
                        title = "No more doses pending",
                        message = "Everything scheduled for today has already been handled."
                    )
                }
            } else {
                items(dueDoses) { dose ->
                    DueDoseCard(
                        dose = dose,
                        onTakeDosePhoto = { onTakeDosePhoto(dose) },
                        onSkip = { onLogDose(dose, DoseStatus.SKIPPED) },
                        onSnooze = { onLogDose(dose, DoseStatus.SNOOZED) }
                    )
                }
            }

            item {
                SectionLabel(text = "Logged today")
            }

            if (todayLoggedDoses.isEmpty()) {
                item {
                    EmptyDoseStateCard(
                        title = "Nothing logged yet",
                        message = "Taken, skipped, and snoozed doses from today will appear here."
                    )
                }
            } else {
                loggedTodaySections.forEach { section ->
                    item {
                        StatusSectionHeader(
                            title = section.title,
                            count = section.logs.size
                        )
                    }

                    items(buildGroupedDoseLogItems(section.logs)) { item ->
                        GroupedDoseLogCard(item = item)
                    }
                }
            }

            item {
                SectionLabel(text = "History")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogHistoryRange.entries.forEach { range ->
                        val isSelected = range == selectedHistoryRange
                        if (isSelected) {
                            Button(
                                onClick = {
                                    selectedHistoryRange = range
                                    expandedMedicationHistoryKey = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = range.label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    selectedHistoryRange = range
                                    expandedMedicationHistoryKey = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = range.label)
                            }
                        }
                    }
                }
            }

            if (filteredRecentLogs.isEmpty()) {
                item {
                    EmptyDoseStateCard(
                        title = "No history in ${selectedHistoryRange.label.lowercase()}",
                        message = "Recent dose activity will appear here once logging starts."
                    )
                }
            } else if (selectedHistoryRange == LogHistoryRange.TODAY) {
                item {
                    InfoCard(title = "Today's activity") {
                        Text(
                            text = "Today's doses are already shown in Logged today above. Choose Week or Month to review older history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "Weekly and monthly history is grouped by medication first. Tap one medicine to see the day-by-day details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(medicationHistorySummaries) { summary ->
                    MedicationHistorySummaryCard(
                        summary = summary,
                        expanded = expandedMedicationHistoryKey == summary.medicationKey,
                        onToggle = {
                            expandedMedicationHistoryKey =
                                if (expandedMedicationHistoryKey == summary.medicationKey) {
                                    null
                                } else {
                                    summary.medicationKey
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(
    summary: TodayDoseSummaryDisplay
) {
    SurfaceCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Outlined.AssignmentTurnedIn,
                size = 44
            )
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClinicTeal
                )
                Text(
                    text = summary.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MediMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = summary.summaryMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        SummaryStatRow(
            leftLabel = "Scheduled",
            leftValue = summary.totalScheduled.toString(),
            rightLabel = "Logged",
            rightValue = summary.completedCount.toString()
        )

        Spacer(modifier = Modifier.height(10.dp))

        SummaryStatRow(
            leftLabel = "Remaining",
            leftValue = summary.remainingCount.toString(),
            rightLabel = "Taken",
            rightValue = summary.takenCount.toString()
        )

        Spacer(modifier = Modifier.height(10.dp))

        SummaryStatRow(
            leftLabel = "Skipped",
            leftValue = summary.skippedCount.toString(),
            rightLabel = "Missed",
            rightValue = summary.missedCount.toString()
        )

        if (summary.snoozedCount > 0 || !summary.nextDoseLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (summary.snoozedCount > 0) {
            StatusChip(
                label = "Snoozed ${summary.snoozedCount}",
                containerColor = WarningLight,
                contentColor = WarningOrange
            )
        }

        if (!summary.nextDoseLabel.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard(
                title = "Next dose",
                containerColor = MediMint,
                contentColor = ClinicTeal
            ) {
                Text(
                    text = summary.nextDoseLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediInk
                )
            }
        }
    }
}

@Composable
private fun SummaryStatRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryStatCard(
            label = leftLabel,
            value = leftValue,
            modifier = Modifier.weight(1f)
        )
        SummaryStatCard(
            label = rightLabel,
            value = rightValue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MediCream
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = statusContentColor(label)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MediMuted
            )
        }
    }
}

@Composable
private fun VerificationStatusCard(
    verification: PendingDoseVerificationDisplay
) {
    val statusColor = when {
        verification.isLikelyMatch -> ClinicTeal
        verification.isManualOverrideAllowed -> Color(0xFFD97706)
        else -> AlertCoral
    }

    InfoCard(
        title = "Live Photo Check"
    ) {
        Text(
            text = "The app is checking whether the live photo looks close to the saved medicine reference.",
            style = MaterialTheme.typography.bodyMedium,
            color = MediInk
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = verification.matchMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Score: ${verification.similarityScore}% | ${verification.debugDetail}",
            style = MaterialTheme.typography.bodySmall,
            color = MediMuted
        )
    }
}

@Composable
private fun VerificationCompareCard(
    verification: PendingDoseVerificationDisplay,
    onConfirmTaken: () -> Unit,
    onRetakeTakenPhoto: () -> Unit
) {
    val bannerColor by animateColorAsState(
        targetValue = when {
            verification.isLikelyMatch -> ClinicTeal.copy(alpha = 0.12f)
            verification.isManualOverrideAllowed -> Color(0xFFF59E0B).copy(alpha = 0.12f)
            else -> AlertCoral.copy(alpha = 0.12f)
        },
        animationSpec = tween(350),
        label = "verificationBanner"
    )
    val statusColor by animateColorAsState(
        targetValue = when {
            verification.isLikelyMatch -> ClinicTeal
            verification.isManualOverrideAllowed -> Color(0xFFD97706)
            else -> AlertCoral
        },
        animationSpec = tween(350),
        label = "verificationStatus"
    )

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MediSurfaceRaised
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Outlined.CameraAlt,
                size = 44
            )
            Column {
                Text(
                    text = "Confirm taken dose",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClinicTeal
                )
                Text(
                    text = "${verification.medicationName} at ${verification.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VerificationPhotoBox(
                label = "Saved reference",
                imageUri = verification.referenceImageUri,
                modifier = Modifier.weight(1f)
            )
            VerificationPhotoBox(
                label = "Live photo now",
                imageUri = verification.capturedImageUri,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = bannerColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when {
                    verification.isLikelyMatch -> Icons.Outlined.CheckCircle
                    verification.isManualOverrideAllowed -> Icons.Outlined.WarningAmber
                    else -> Icons.Outlined.LocalHospital
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = verification.matchMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Score: ${verification.similarityScore}% | ${verification.debugDetail}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MediMuted
                )
            }
        }

        if (verification.isManualOverrideAllowed && !verification.isLikelyMatch) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "The photo looks similar but not identical. Different lighting or angle can cause this. If you are sure it is the right medicine, you can still confirm.",
                style = MaterialTheme.typography.bodySmall,
                color = MediMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            verification.isLikelyMatch -> {
                Button(
                    onClick = onConfirmTaken,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Confirm Taken")
                }
            }

            verification.isManualOverrideAllowed -> {
                FilledTonalButton(
                    onClick = onConfirmTaken,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.18f),
                        contentColor = Color(0xFFB45309)
                    )
                ) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Confirm Anyway (I checked)")
                }
            }

            else -> {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(text = "Confirm Taken")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onRetakeTakenPhoto,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Retake Photo")
        }
        }
    }
}

@Composable
private fun VerificationPhotoBox(
    label: String,
    imageUri: String,
    modifier: Modifier = Modifier,
    height: Dp = 110.dp
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(imageUri))
                ?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MediMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DueDoseCard(
    dose: DoseLoggingItem,
    onTakeDosePhoto: () -> Unit,
    onSkip: () -> Unit,
    onSnooze: () -> Unit
) {
    SurfaceCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Outlined.Schedule,
                size = 44
            )
            Column {
                Text(
                    text = dose.medicationName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Scheduled ${dose.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(label = dose.frequencyLabel)
            if (dose.availabilityMessage.isNotBlank()) {
                StatusChip(
                    label = dose.availabilityMessage,
                    containerColor = WarningLight,
                    contentColor = WarningOrange
                )
            }
            if (!dose.todayStatusLabel.isNullOrBlank()) {
                DoseStatusChip(label = dose.todayStatusLabel)
            }
        }

        if (dose.stockLabel.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            StatusChip(
                label = "Remaining: ${dose.stockLabel}",
                containerColor = if (dose.isLowStock) DangerLight else SuccessLight,
                contentColor = if (dose.isLowStock) DangerRed else SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onTakeDosePhoto,
            modifier = Modifier.fillMaxWidth(),
            enabled = dose.isActionAllowed,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Take Live Photo To Confirm")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            enabled = dose.isActionAllowed,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Skip this dose")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSnooze,
            modifier = Modifier.fillMaxWidth(),
            enabled = dose.isActionAllowed,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange)
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsPaused,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "Snooze for later")
        }
    }
}

@Composable
private fun EmptyDoseStateCard(
    title: String,
    message: String
) {
    SurfaceCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = Icons.Outlined.CheckCircle,
                size = 44
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClinicTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediMuted
                )
            }
        }
    }
}

private fun sampleDoseLoggingItems(): List<DoseLoggingItem> {
    return listOf(
        DoseLoggingItem(
            doseScheduleId = 1,
            medicationId = 1,
            medicationName = "Paracetamol",
            scheduledTime = "08:00 AM",
            frequencyLabel = "twice daily"
        ),
        DoseLoggingItem(
            doseScheduleId = 2,
            medicationId = 2,
            medicationName = "Cough Syrup",
            scheduledTime = "09:00 PM",
            frequencyLabel = "once daily"
        )
    )
}

private fun sampleDoseLogDisplayItems(): List<DoseLogDisplayItem> {
    return listOf(
        DoseLogDisplayItem(
            medicationName = "Paracetamol",
            scheduledTime = "08:00 AM",
            logDate = "2026-04-18",
            status = DoseStatus.TAKEN,
            takenAt = "2026-04-18 08:05 AM",
            hasVerificationPhoto = true,
            imageUri = null
        ),
        DoseLogDisplayItem(
            medicationName = "Cough Syrup",
            scheduledTime = "09:00 PM",
            logDate = "2026-04-17",
            status = DoseStatus.SNOOZED,
            takenAt = null
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun DoseLoggingScreenPreview() {
    MediRemindTheme {
        DoseLoggingScreen()
    }
}

@Composable
private fun GroupedDoseLogCard(
    item: GroupedDoseLogItem
) {
    SurfaceCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = when {
                    item.statusSummary.contains("Taken") -> Icons.Outlined.CheckCircle
                    item.statusSummary.contains("Missed") -> Icons.Outlined.WarningAmber
                    else -> Icons.Outlined.History
                },
                size = 42
            )
            Column {
                Text(
                    text = item.medicationName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Times: ${item.scheduledTimes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(label = formatHistoryDate(item.logDate))
            StatusChip(
                label = item.statusSummary,
                containerColor = statusContainerColor(item.statusSummary),
                contentColor = statusContentColor(item.statusSummary)
            )
        }

        if (item.takenAtEntries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            item.takenAtEntries.forEach { entry ->
                Text(
                    text = "Recorded: $entry",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (item.hasVerificationPhoto) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verification photo saved",
                style = MaterialTheme.typography.bodySmall,
                color = ClinicTeal
            )

            if (!item.imageUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                VerificationImagePreview(
                    imageUri = item.imageUri,
                    size = 72.dp
                )
            }
        }
    }
}

@Composable
private fun MedicationHistorySummaryCard(
    summary: MedicationHistorySummary,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    SurfaceCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = when {
                    summary.missedCount > 0 -> Icons.Outlined.WarningAmber
                    summary.takenCount > 0 -> Icons.Outlined.CheckCircle
                    else -> Icons.Outlined.History
                },
                size = 42
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.medicationName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (summary.scheduledTimes.isNotEmpty()) {
                    Text(
                        text = "Times: ${summary.scheduledTimes.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DoseStatusChip(label = "Taken ${summary.takenCount}")
            DoseStatusChip(label = "Missed ${summary.missedCount}")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DoseStatusChip(label = "Skipped ${summary.skippedCount}")
            DoseStatusChip(label = "Snoozed ${summary.snoozedCount}")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (expanded) {
                    "Hide day-by-day details"
                } else {
                    "Show day-by-day details"
                }
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            summary.dateSections.forEach { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ClinicTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                section.items.forEach { item ->
                    MedicationHistoryDetailCard(item = item)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MedicationHistoryDetailCard(
    item: GroupedDoseLogItem
) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(
                    label = item.statusSummary,
                    containerColor = statusContainerColor(item.statusSummary),
                    contentColor = statusContentColor(item.statusSummary)
                )
                StatusChip(label = "Times ${item.scheduledTimes.joinToString(", ")}")
            }

            if (item.takenAtEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                item.takenAtEntries.forEach { entry ->
                    Text(
                        text = "Recorded: $entry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediMuted
                    )
                }
            }

            if (item.hasVerificationPhoto) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Verification photo saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClinicTeal
                )
                if (!item.imageUri.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    VerificationImagePreview(
                        imageUri = item.imageUri,
                        size = 72.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationImagePreview(
    imageUri: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        runCatching {
            context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        OutlinedCard(
            shape = RoundedCornerShape(14.dp),
            modifier = modifier
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Dose verification photo",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    } else {
        Text(
            text = "Photo preview unavailable",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatusSectionHeader(
    title: String,
    count: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = when (title) {
                "Taken" -> Icons.Outlined.CheckCircle
                "Missed" -> Icons.Outlined.WarningAmber
                "Skipped" -> Icons.Outlined.LocalHospital
                else -> Icons.Outlined.NotificationsPaused
            },
            size = 34
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = statusContentColor(title)
        )
        StatusChip(
            label = count.toString(),
            containerColor = statusContainerColor(title),
            contentColor = statusContentColor(title)
        )
    }
}

@Composable
private fun DoseStatusChip(label: String) {
    StatusChip(
        label = label,
        containerColor = statusContainerColor(label),
        contentColor = statusContentColor(label)
    )
}

private fun statusContainerColor(label: String): Color {
    val normalized = label.lowercase()
    return when {
        "taken" in normalized || "complete" in normalized || "success" in normalized -> SuccessLight
        "missed" in normalized || "skip" in normalized || "skipped" in normalized -> DangerLight
        "snooze" in normalized || "snoozed" in normalized || "upcoming" in normalized ||
            "available" in normalized || "later" in normalized || "remaining" in normalized -> WarningLight
        else -> MediPrimaryLight
    }
}

private fun statusContentColor(label: String): Color {
    val normalized = label.lowercase()
    return when {
        "taken" in normalized || "complete" in normalized || "success" in normalized -> SuccessGreen
        "missed" in normalized || "skip" in normalized || "skipped" in normalized -> DangerRed
        "snooze" in normalized || "snoozed" in normalized || "upcoming" in normalized ||
            "available" in normalized || "later" in normalized || "remaining" in normalized -> WarningOrange
        else -> MediPrimaryDark
    }
}

private enum class LogHistoryRange(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month")
}

private data class DoseLogSection(
    val title: String,
    val logs: List<DoseLogDisplayItem>
)

private data class GroupedDoseLogItem(
    val medicationName: String,
    val logDate: String,
    val scheduledTimes: List<String>,
    val statusSummary: String,
    val takenAtEntries: List<String>,
    val imageUri: String? = null,
    val hasVerificationPhoto: Boolean = false
)

private data class GroupedDoseLogSection(
    val title: String,
    val items: List<GroupedDoseLogItem>
)

private data class MedicationHistorySummary(
    val medicationKey: String,
    val medicationName: String,
    val scheduledTimes: List<String>,
    val takenCount: Int,
    val missedCount: Int,
    val skippedCount: Int,
    val snoozedCount: Int,
    val dateSections: List<GroupedDoseLogSection>
)

private fun filterDoseLogsForRange(
    logs: List<DoseLogDisplayItem>,
    range: LogHistoryRange
): List<DoseLogDisplayItem> {
    val today = Calendar.getInstance()
    return logs
        .filter { log ->
            val calendar = parseLogDate(log.logDate) ?: return@filter false
            when (range) {
                LogHistoryRange.TODAY -> sameDay(calendar, today)
                LogHistoryRange.WEEK -> sameWeek(calendar, today)
                LogHistoryRange.MONTH -> sameMonth(calendar, today)
            }
        }
        .sortedWith(doseLogDisplayComparator())
}

private fun buildStatusSections(
    logs: List<DoseLogDisplayItem>
): List<DoseLogSection> {
    return listOf(
        DoseStatus.TAKEN,
        DoseStatus.MISSED,
        DoseStatus.SKIPPED,
        DoseStatus.SNOOZED
    ).mapNotNull { status ->
        val groupedLogs = logs
            .filter { it.status == status }
            .sortedWith(doseLogDisplayComparator())

        if (groupedLogs.isEmpty()) {
            null
        } else {
            DoseLogSection(
                title = statusSectionTitle(status),
                logs = groupedLogs
            )
        }
    }
}

private fun buildGroupedDateSections(
    logs: List<DoseLogDisplayItem>
): List<GroupedDoseLogSection> {
    return logs
        .groupBy { it.logDate }
        .toList()
        .sortedByDescending { (date, _) ->
            parseLogDate(date)?.timeInMillis ?: Long.MIN_VALUE
        }
        .map { (date, dateLogs) ->
            GroupedDoseLogSection(
                title = formatFullDateHeader(date),
                items = buildGroupedDoseLogItems(dateLogs)
            )
        }
}

private fun buildMedicationHistorySummaries(
    logs: List<DoseLogDisplayItem>
): List<MedicationHistorySummary> {
    return logs
        .groupBy { it.medicationName.trim().lowercase() }
        .map { (medicationKey, medicationLogs) ->
            val sortedLogs = medicationLogs.sortedWith(doseLogDisplayComparator())
            MedicationHistorySummary(
                medicationKey = medicationKey,
                medicationName = sortedLogs.first().medicationName,
                scheduledTimes = sortedLogs
                    .map { it.scheduledTime }
                    .distinct()
                    .sortedBy { parseTimeToMinutesForLogs(it) ?: Int.MAX_VALUE },
                takenCount = sortedLogs.count { it.status == DoseStatus.TAKEN },
                missedCount = sortedLogs.count { it.status == DoseStatus.MISSED },
                skippedCount = sortedLogs.count { it.status == DoseStatus.SKIPPED },
                snoozedCount = sortedLogs.count { it.status == DoseStatus.SNOOZED },
                dateSections = buildGroupedDateSections(sortedLogs)
            )
        }
        .sortedWith(
            compareByDescending<MedicationHistorySummary> { it.missedCount }
                .thenBy { firstTimeMinutes(it.scheduledTimes) ?: Int.MAX_VALUE }
                .thenBy { it.medicationName.lowercase() }
        )
}

private fun buildGroupedDoseLogItems(
    logs: List<DoseLogDisplayItem>
): List<GroupedDoseLogItem> {
    return logs
        .groupBy { it.medicationName.trim().lowercase() }
        .values
        .map { medicationLogs ->
            val sortedLogs = medicationLogs.sortedWith(
                compareBy<DoseLogDisplayItem> { parseTimeToMinutesForLogs(it.scheduledTime) ?: Int.MAX_VALUE }
                    .thenBy { statusSortOrder(it.status) }
            )
            val latestPhotoLog = sortedLogs.lastOrNull { !it.imageUri.isNullOrBlank() }
            GroupedDoseLogItem(
                medicationName = sortedLogs.first().medicationName,
                logDate = sortedLogs.first().logDate,
                scheduledTimes = sortedLogs.map { it.scheduledTime },
                statusSummary = buildStatusSummary(sortedLogs),
                takenAtEntries = sortedLogs.mapNotNull { log ->
                    log.takenAt?.let { "${log.scheduledTime}: $it" }
                },
                imageUri = latestPhotoLog?.imageUri,
                hasVerificationPhoto = sortedLogs.any { it.hasVerificationPhoto }
            )
        }
        .sortedWith(
            compareBy<GroupedDoseLogItem>(
                { firstTimeMinutes(it.scheduledTimes) ?: Int.MAX_VALUE },
                { it.medicationName.lowercase() }
            )
        )
}

private fun buildStatusSummary(
    logs: List<DoseLogDisplayItem>
): String {
    val counts = linkedMapOf(
        DoseStatus.TAKEN to logs.count { it.status == DoseStatus.TAKEN },
        DoseStatus.MISSED to logs.count { it.status == DoseStatus.MISSED },
        DoseStatus.SKIPPED to logs.count { it.status == DoseStatus.SKIPPED },
        DoseStatus.SNOOZED to logs.count { it.status == DoseStatus.SNOOZED }
    )

    return counts.mapNotNull { (status, count) ->
        if (count == 0) null else "${statusSectionTitle(status)} $count"
    }.joinToString(" | ")
}

private fun firstTimeMinutes(times: List<String>): Int? {
    return times.firstOrNull()?.let { parseTimeToMinutesForLogs(it) }
}

private fun doseLogDisplayComparator(): Comparator<DoseLogDisplayItem> {
    return compareBy<DoseLogDisplayItem>(
        { statusSortOrder(it.status) },
        { parseTimeToMinutesForLogs(it.scheduledTime) ?: Int.MAX_VALUE },
        { it.medicationName.lowercase() }
    )
}

private fun statusSortOrder(status: DoseStatus): Int {
    return when (status) {
        DoseStatus.TAKEN -> 0
        DoseStatus.MISSED -> 1
        DoseStatus.SKIPPED -> 2
        DoseStatus.SNOOZED -> 3
    }
}

private fun statusSectionTitle(status: DoseStatus): String {
    return when (status) {
        DoseStatus.TAKEN -> "Taken"
        DoseStatus.MISSED -> "Missed"
        DoseStatus.SKIPPED -> "Skipped"
        DoseStatus.SNOOZED -> "Snoozed"
    }
}

private fun parseLogDate(value: String): Calendar? {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(value) ?: return null
        Calendar.getInstance().apply { time = date }
    } catch (_: Exception) {
        null
    }
}

private fun sameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun sameWeek(first: Calendar, second: Calendar): Boolean {
    val differenceInDays = ((second.timeInMillis - first.timeInMillis) / (24 * 60 * 60 * 1000L)).toInt()
    return differenceInDays in 0..6
}

private fun sameMonth(first: Calendar, second: Calendar): Boolean {
    val differenceInDays = ((second.timeInMillis - first.timeInMillis) / (24 * 60 * 60 * 1000L)).toInt()
    return differenceInDays in 0..29
}

private fun formatHistoryDate(logDate: String): String {
    val parsedDate = parseLogDate(logDate) ?: return logDate
    val today = Calendar.getInstance()
    return when {
        sameDay(parsedDate, today) -> "Today"
        sameWeek(parsedDate, today) -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(parsedDate.time)
        }
        else -> {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsedDate.time)
        }
    }
}

private fun formatFullDateHeader(logDate: String): String {
    val parsedDate = parseLogDate(logDate) ?: return logDate
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val readableDate = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(parsedDate.time)

    return when {
        sameDay(parsedDate, today) -> "Today - $readableDate"
        sameDay(parsedDate, yesterday) -> "Yesterday - $readableDate"
        else -> readableDate
    }
}

private fun parseTimeToMinutesForLogs(value: String): Int? {
    return try {
        val parser = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = parser.parse(value) ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    } catch (_: Exception) {
        null
    }
}
