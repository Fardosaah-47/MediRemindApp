package com.example.mediremind.ui.screen.reminder

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.ui.theme.MediRemindTheme
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
    val matchMessage: String,
    val similarityScore: Int
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
    val scrollState = rememberScrollState()
    var selectedHistoryRange by rememberSaveable { mutableStateOf(LogHistoryRange.WEEK) }
    val filteredRecentLogs = remember(recentLogs, selectedHistoryRange) {
        filterDoseLogsForRange(recentLogs, selectedHistoryRange)
    }
    val loggedTodaySections = remember(todayLoggedDoses) {
        buildStatusSections(todayLoggedDoses)
    }
    val historyDateSections = remember(filteredRecentLogs) {
        buildDateSections(filteredRecentLogs)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Dose Logging",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Record whether each medication was taken, skipped, or snoozed.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (todaySummary != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todaySummary.dateLabel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Scheduled: ${todaySummary.totalScheduled}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Logged: ${todaySummary.completedCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Remaining: ${todaySummary.remainingCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Taken: ${todaySummary.takenCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Skipped: ${todaySummary.skippedCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (todaySummary.snoozedCount > 0) {
                        Text(
                            text = "Snoozed: ${todaySummary.snoozedCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (todaySummary.missedCount > 0) {
                        Text(
                            text = "Missed: ${todaySummary.missedCount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (!todaySummary.nextDoseLabel.isNullOrBlank()) {
                        Text(
                            text = "Next: ${todaySummary.nextDoseLabel}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todaySummary.summaryMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (pendingDoseVerification != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Live Photo Check",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Compare the saved medicine photo with the live photo you just captured, then confirm.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pendingDoseVerification.matchMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (pendingDoseVerification.isLikelyMatch) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )

                    Text(
                        text = "Similarity check: ${pendingDoseVerification.similarityScore}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

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
                    Text(
                        text = "Confirm Taken Dose",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${pendingDoseVerification.medicationName} at ${pendingDoseVerification.scheduledTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Expected medicine",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    VerificationImagePreview(imageUri = pendingDoseVerification.referenceImageUri)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Live photo now",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    VerificationImagePreview(imageUri = pendingDoseVerification.capturedImageUri)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (pendingDoseVerification.isLikelyMatch) {
                            "This looks close enough to the saved reference."
                        } else {
                            "This does not look close enough to the saved reference yet."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (pendingDoseVerification.isLikelyMatch) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onConfirmTaken,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = pendingDoseVerification.isLikelyMatch
                    ) {
                        Text(text = "Confirm Taken")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRetakeTakenPhoto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Retake Photo")
                    }
                }
            }
        } else if (statusMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Dose Update",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Back To Home")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Remaining Today",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (dueDoses.isEmpty()) {
            Text(
                text = "No more scheduled doses for today.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            dueDoses.forEach { dose ->
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
                            text = dose.medicationName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Scheduled: ${dose.scheduledTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Frequency: ${dose.frequencyLabel}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (dose.availabilityMessage.isNotBlank()) {
                            Text(
                                text = "Status: ${dose.availabilityMessage}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!dose.todayStatusLabel.isNullOrBlank()) {
                            Text(
                                text = "Today: ${dose.todayStatusLabel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onTakeDosePhoto(dose) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = dose.isActionAllowed
                        ) {
                            Text(
                                text = "Take Live Photo To Confirm"
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onLogDose(dose, DoseStatus.SKIPPED) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = dose.isActionAllowed
                        ) {
                            Text(text = "Skipped")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onLogDose(dose, DoseStatus.SNOOZED) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = dose.isActionAllowed
                        ) {
                            Text(text = "Snoozed")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Logged Today",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (todayLoggedDoses.isEmpty()) {
            Text(
                text = "Nothing has been logged yet today.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            loggedTodaySections.forEach { section ->
                Text(
                    text = "${section.title} (${section.logs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                section.logs.forEach { log ->
                    DoseLogCard(log = log)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "History",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogHistoryRange.entries.forEach { range ->
                val isSelected = range == selectedHistoryRange
                if (isSelected) {
                    Button(
                        onClick = { selectedHistoryRange = range },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = range.label)
                    }
                } else {
                    OutlinedButton(
                        onClick = { selectedHistoryRange = range },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = range.label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredRecentLogs.isEmpty()) {
            Text(
                text = "No dose logs in ${selectedHistoryRange.label.lowercase()} yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            historyDateSections.forEach { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                section.logs.forEach { log ->
                    DoseLogCard(log = log)
                }
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
private fun DoseLogCard(
    log: DoseLogDisplayItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = log.medicationName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Scheduled: ${log.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Date: ${formatHistoryDate(log.logDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!log.takenAt.isNullOrBlank()) {
                    Text(
                        text = "Recorded: ${log.takenAt}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (log.hasVerificationPhoto) {
                    Text(
                        text = "Verification photo saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!log.imageUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VerificationImagePreview(
                            imageUri = log.imageUri,
                            size = 72.dp
                        )
                    }
                }
            }

            Text(
                text = log.status.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VerificationImagePreview(
    imageUri: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp
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
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Dose verification photo",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        Text(
            text = "Photo preview unavailable",
            style = MaterialTheme.typography.bodySmall
        )
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

private fun buildDateSections(
    logs: List<DoseLogDisplayItem>
): List<DoseLogSection> {
    return logs
        .groupBy { it.logDate }
        .toList()
        .sortedByDescending { (date, _) ->
            parseLogDate(date)?.timeInMillis ?: Long.MIN_VALUE
        }
        .map { (date, dateLogs) ->
            DoseLogSection(
                title = formatFullDateHeader(date),
                logs = dateLogs.sortedWith(doseLogDisplayComparator())
            )
        }
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
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.WEEK_OF_YEAR) == second.get(Calendar.WEEK_OF_YEAR)
}

private fun sameMonth(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
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
