package com.example.mediremind.ui.screen.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.Medication
import com.example.mediremind.ui.components.InfoCard
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.SurfaceCard
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediRemindTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DoseScheduleFormScreen(
    modifier: Modifier = Modifier,
    medications: List<Medication> = emptyList(),
    existingSchedule: DoseSchedule? = null,
    onSaveSchedules: (List<DoseSchedule>) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val selectedMedicationId = remember(existingSchedule?.id) {
        mutableStateOf<Long?>(existingSchedule?.medicationId)
    }
    val selectedFrequency = remember(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.frequency ?: DoseFrequency.ONCE_DAILY)
    }
    val firstReminderTime = remember(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.time ?: defaultTimesFor(DoseFrequency.ONCE_DAILY).first())
    }
    val secondReminderTime = remember(existingSchedule?.id) {
        mutableStateOf("")
    }
    val thirdReminderTime = remember(existingSchedule?.id) {
        mutableStateOf("")
    }
    val startDate = remember(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.startDate ?: addDaysToDate(todayDateOnly(), 1))
    }
    val endDate = remember(existingSchedule?.id) {
        mutableStateOf(existingSchedule?.endDate ?: addDaysToDate(todayDateOnly(), 29))
    }
    val medicationMenuExpanded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isEditing = existingSchedule != null

    val selectedMedicationName = medications.firstOrNull { it.id == selectedMedicationId.value }?.name
        ?: "Tap to choose medication"
    val firstTimeLabel = if (isEditing) "Reminder Time" else firstTimeLabel(selectedFrequency.value)
    val secondTimeLabel = secondTimeLabel(selectedFrequency.value)
    val thirdTimeLabel = thirdTimeLabel(selectedFrequency.value)
    val hasValidTreatmentPeriod = isDateRangeValid(
        startDate = startDate.value,
        endDate = endDate.value
    )
    val selectedMedication = medications.firstOrNull { it.id == selectedMedicationId.value }
    val estimatedScheduleSummary = buildEstimatedScheduleSummary(
        medication = selectedMedication,
        frequency = selectedFrequency.value,
        startDate = startDate.value,
        firstReminderTime = firstReminderTime.value,
        secondReminderTime = secondReminderTime.value,
        thirdReminderTime = thirdReminderTime.value
    )
    val frequencyMismatchWarning = selectedMedication?.dosage?.let { dosage ->
        buildFrequencyMismatchWarning(
            dosageText = dosage,
            selectedFrequency = selectedFrequency.value
        )
    }

    LaunchedEffect(selectedFrequency.value, existingSchedule?.id) {
        if (!isEditing) {
            applyDefaultTimes(
                frequency = selectedFrequency.value,
                firstReminderTime = firstReminderTime.value,
                secondReminderTime = secondReminderTime.value,
                thirdReminderTime = thirdReminderTime.value,
                onFirstTime = { firstReminderTime.value = it },
                onSecondTime = { secondReminderTime.value = it },
                onThirdTime = { thirdReminderTime.value = it }
            )
        }
    }

    Scaffold(
        topBar = {
            MediRemindTopBar(
                title = if (isEditing) "Edit Schedule" else "Dose Schedule",
                onBackClick = onCancel
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
            item {
                SectionLabel(text = "MEDICATION")
                Spacer(modifier = Modifier.height(8.dp))

                if (medications.isEmpty()) {
                    SurfaceCard {
                        Text(
                            text = "Save at least one medication first before creating a schedule.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    SurfaceCard {
                        ExposedDropdownMenuBox(
                            expanded = medicationMenuExpanded.value,
                            onExpandedChange = { medicationMenuExpanded.value = !medicationMenuExpanded.value }
                        ) {
                            OutlinedTextField(
                                value = selectedMedicationName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Medication") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = medicationMenuExpanded.value)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = medicationMenuExpanded.value,
                                onDismissRequest = { medicationMenuExpanded.value = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                medications.forEach { medication ->
                                    DropdownMenuItem(
                                        text = { Text(medication.name) },
                                        onClick = {
                                            selectedMedicationId.value = medication.id
                                            medicationMenuExpanded.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel(text = "FREQUENCY")
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditing) {
                    InfoCard(
                        title = "Saved frequency"
                    ) {
                        Text(
                            text = selectedFrequency.value.name.lowercase().replace('_', ' '),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To change once daily, twice daily, or three times daily, update the medication instructions in Medication Setup. The linked schedule pattern will refresh there automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DoseFrequency.entries.forEach { frequency ->
                            val isSelected = selectedFrequency.value == frequency
                            val label = frequency.name.lowercase().replace('_', ' ')
                            if (isSelected) {
                                Button(
                                    onClick = { selectedFrequency.value = frequency },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = label)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedFrequency.value = frequency },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = label)
                                }
                            }
                        }
                    }
                }

                if (!isEditing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Default times load automatically. You can still change them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!frequencyMismatchWarning.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = frequencyMismatchWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                SectionLabel(text = "TREATMENT PERIOD")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                openDatePicker(
                                    context = context,
                                    initialValue = startDate.value
                                ) { selectedDate ->
                                    startDate.value = selectedDate
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Start Date: ${formatDateForDisplay(startDate.value)}")
                        }

                        OutlinedButton(
                            onClick = {
                                openDatePicker(
                                    context = context,
                                    initialValue = endDate.value
                                ) { selectedDate ->
                                    endDate.value = selectedDate
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "End Date: ${formatDateForDisplay(endDate.value)}")
                        }

                        if (!hasValidTreatmentPeriod) {
                            Text(
                                text = "End date must be the same day as the start date or later.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                SectionLabel(text = "REMINDER TIMES")
                Spacer(modifier = Modifier.height(8.dp))
                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                openTimePicker(
                                    context = context,
                                    initialValue = firstReminderTime.value
                                ) { selectedTime ->
                                    firstReminderTime.value = selectedTime
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (firstReminderTime.value.isBlank()) {
                                    "Pick $firstTimeLabel"
                                } else {
                                    "$firstTimeLabel: ${firstReminderTime.value}"
                                }
                            )
                        }

                        if (!isEditing && (
                                selectedFrequency.value == DoseFrequency.TWICE_DAILY ||
                                    selectedFrequency.value == DoseFrequency.THREE_TIMES_DAILY
                                )
                        ) {
                            OutlinedButton(
                                onClick = {
                                    openTimePicker(
                                        context = context,
                                        initialValue = secondReminderTime.value
                                    ) { selectedTime ->
                                        secondReminderTime.value = selectedTime
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (secondReminderTime.value.isBlank()) {
                                        "Pick $secondTimeLabel"
                                    } else {
                                        "$secondTimeLabel: ${secondReminderTime.value}"
                                    }
                                )
                            }
                        }

                        if (!isEditing && selectedFrequency.value == DoseFrequency.THREE_TIMES_DAILY) {
                            OutlinedButton(
                                onClick = {
                                    openTimePicker(
                                        context = context,
                                        initialValue = thirdReminderTime.value
                                    ) { selectedTime ->
                                        thirdReminderTime.value = selectedTime
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (thirdReminderTime.value.isBlank()) {
                                        "Pick $thirdTimeLabel"
                                    } else {
                                        "$thirdTimeLabel: ${thirdReminderTime.value}"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            estimatedScheduleSummary?.let { summary ->
                item {
                    InfoCard(
                        title = "Estimated Plan"
                    ) {
                        EstimatedPlanRow(label = "Starts", value = summary.startLabel)
                        Spacer(modifier = Modifier.height(8.dp))
                        EstimatedPlanRow(label = "Expected End", value = summary.endLabel)
                        Spacer(modifier = Modifier.height(8.dp))
                        EstimatedPlanRow(label = "Refill Alert", value = summary.refillLabel)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "This timeline may shift later if real dose logs show skipped or missed doses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val medicationId = selectedMedicationId.value
                            if (medicationId != null) {
                                val times = buildList {
                                    if (firstReminderTime.value.isNotBlank()) add(firstReminderTime.value)
                                    if (secondReminderTime.value.isNotBlank()) add(secondReminderTime.value)
                                    if (thirdReminderTime.value.isNotBlank()) add(thirdReminderTime.value)
                                }

                                val schedules = times.map { time ->
                                    DoseSchedule(
                                        id = existingSchedule?.id ?: 0,
                                        medicationId = medicationId,
                                        time = time,
                                        frequency = selectedFrequency.value,
                                        startDate = startDate.value,
                                        endDate = endDate.value
                                    )
                                }

                                if (schedules.isNotEmpty() && hasValidTreatmentPeriod) {
                                    onSaveSchedules(schedules)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasValidTreatmentPeriod
                    ) {
                        Text(text = if (isEditing) "Update Schedule" else "Save Schedule")
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DoseScheduleFormScreenPreview() {
    MediRemindTheme {
        DoseScheduleFormScreen()
    }
}

private fun openTimePicker(
    context: Context,
    initialValue: String,
    onTimeSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val initialHourMinute = parseHourMinute(initialValue)
    val hour = initialHourMinute?.first ?: calendar.get(Calendar.HOUR_OF_DAY)
    val minute = initialHourMinute?.second ?: calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(formatTime(selectedHour, selectedMinute))
        },
        hour,
        minute,
        false
    ).show()
}

private fun openDatePicker(
    context: Context,
    initialValue: String,
    onDateSelected: (String) -> Unit
) {
    val calendar = parseDateValue(initialValue) ?: Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(dateOnlyFormatter().format(selectedCalendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun parseHourMinute(value: String): Pair<Int, Int>? {
    if (value.isBlank()) return null

    return try {
        val parser = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = parser.parse(value) ?: return null
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
    } catch (_: Exception) {
        null
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
}

private fun todayDateOnly(): String {
    return dateOnlyFormatter().format(Date())
}

private fun addDaysToDate(value: String, days: Int): String {
    val calendar = parseDateValue(value) ?: Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return dateOnlyFormatter().format(calendar.time)
}

private fun isDateRangeValid(startDate: String, endDate: String): Boolean {
    val start = parseDateValue(startDate) ?: return false
    val end = parseDateValue(endDate) ?: return false
    return !end.before(start)
}

private fun parseDateValue(value: String): Calendar? {
    if (value.isBlank()) return null

    return try {
        val parsedDate = dateOnlyFormatter().parse(value) ?: return null
        Calendar.getInstance().apply { time = parsedDate }
    } catch (_: Exception) {
        null
    }
}

private fun formatDateForDisplay(value: String): String {
    val calendar = parseDateValue(value) ?: return value
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun dateOnlyFormatter(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}

private data class EstimatedScheduleSummary(
    val startLabel: String,
    val endLabel: String,
    val refillLabel: String
)

@Composable
private fun EstimatedPlanRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun defaultTimesFor(frequency: DoseFrequency): List<String> {
    return when (frequency) {
        DoseFrequency.ONCE_DAILY -> listOf("09:00 AM")
        DoseFrequency.TWICE_DAILY -> listOf("09:00 AM", "09:00 PM")
        DoseFrequency.THREE_TIMES_DAILY -> listOf("09:00 AM", "01:00 PM", "09:00 PM")
        DoseFrequency.WEEKLY -> listOf("09:00 AM")
        DoseFrequency.AS_NEEDED -> listOf("09:00 AM")
    }
}

private fun applyDefaultTimes(
    frequency: DoseFrequency,
    firstReminderTime: String,
    secondReminderTime: String,
    thirdReminderTime: String,
    onFirstTime: (String) -> Unit,
    onSecondTime: (String) -> Unit,
    onThirdTime: (String) -> Unit
) {
    val defaults = defaultTimesFor(frequency)

    if (firstReminderTime.isBlank()) {
        onFirstTime(defaults.getOrElse(0) { "" })
    }

    when (frequency) {
        DoseFrequency.TWICE_DAILY -> {
            if (secondReminderTime.isBlank()) {
                onSecondTime(defaults.getOrElse(1) { "" })
            }
            onThirdTime("")
        }
        DoseFrequency.THREE_TIMES_DAILY -> {
            if (secondReminderTime.isBlank()) {
                onSecondTime(defaults.getOrElse(1) { "" })
            }
            if (thirdReminderTime.isBlank()) {
                onThirdTime(defaults.getOrElse(2) { "" })
            }
        }
        else -> {
            onSecondTime("")
            onThirdTime("")
        }
    }
}

private fun buildEstimatedScheduleSummary(
    medication: Medication?,
    frequency: DoseFrequency,
    startDate: String,
    firstReminderTime: String,
    secondReminderTime: String,
    thirdReminderTime: String
): EstimatedScheduleSummary? {
    val stockAmount = medication?.currentStockAmount ?: return null
    val refillAlertAt = medication.refillAlertAt
    if (stockAmount <= 0.0) return null

    val remindersPerDay = countPlannedRemindersPerDay(
        frequency = frequency,
        firstReminderTime = firstReminderTime,
        secondReminderTime = secondReminderTime,
        thirdReminderTime = thirdReminderTime
    )
    if (remindersPerDay <= 0) return null

    val startCalendar = parseDateValue(startDate) ?: return null
    val dailyAmountUsed = (medication.amountPerDose * remindersPerDay)
        .takeIf { it > 0.0 }
        ?: remindersPerDay.toDouble()
    val estimatedDaysOfSupply = kotlin.math.ceil(stockAmount / dailyAmountUsed).toInt()
    val estimatedEndCalendar = (startCalendar.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, estimatedDaysOfSupply - 1)
    }

    val dosesUntilRefill = (stockAmount - refillAlertAt).coerceAtLeast(0.0)
    val estimatedRefillDays = kotlin.math.floor(dosesUntilRefill / dailyAmountUsed).toInt()
    val refillCalendar = (startCalendar.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, estimatedRefillDays)
    }

    return EstimatedScheduleSummary(
        startLabel = buildFriendlyStartLabel(startCalendar),
        endLabel = "${formatFriendlyDate(estimatedEndCalendar)} (${daysBetweenInclusive(startCalendar, estimatedEndCalendar)} day(s) supply)",
        refillLabel = formatFriendlyDate(refillCalendar)
    )
}

private fun countPlannedRemindersPerDay(
    frequency: DoseFrequency,
    firstReminderTime: String,
    secondReminderTime: String,
    thirdReminderTime: String
): Int {
    return when (frequency) {
        DoseFrequency.ONCE_DAILY, DoseFrequency.WEEKLY, DoseFrequency.AS_NEEDED -> if (firstReminderTime.isBlank()) 0 else 1
        DoseFrequency.TWICE_DAILY -> listOf(firstReminderTime, secondReminderTime).count { it.isNotBlank() }
        DoseFrequency.THREE_TIMES_DAILY -> listOf(firstReminderTime, secondReminderTime, thirdReminderTime).count { it.isNotBlank() }
    }
}

private fun buildFriendlyStartLabel(startCalendar: Calendar): String {
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    return when {
        sameDay(startCalendar, today) -> "Today, ${formatFriendlyDate(startCalendar)}"
        sameDay(startCalendar, tomorrow) -> "Tomorrow, ${formatFriendlyDate(startCalendar)}"
        else -> formatFriendlyDate(startCalendar)
    }
}

private fun formatFriendlyDate(calendar: Calendar): String {
    return SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun daysBetweenInclusive(start: Calendar, end: Calendar): Int {
    val millisecondsPerDay = 24 * 60 * 60 * 1000L
    val difference = (end.timeInMillis - start.timeInMillis) / millisecondsPerDay
    return difference.toInt() + 1
}

private fun buildFrequencyMismatchWarning(
    dosageText: String,
    selectedFrequency: DoseFrequency
): String? {
    val normalized = dosageText.lowercase()
    val suggestedFrequency = when {
        "three times daily" in normalized || "3 times daily" in normalized -> DoseFrequency.THREE_TIMES_DAILY
        "twice daily" in normalized || "2 times daily" in normalized -> DoseFrequency.TWICE_DAILY
        "once daily" in normalized || "every morning" in normalized || "daily" in normalized -> DoseFrequency.ONCE_DAILY
        "weekly" in normalized || "once weekly" in normalized -> DoseFrequency.WEEKLY
        "as needed" in normalized || "when needed" in normalized -> DoseFrequency.AS_NEEDED
        else -> null
    } ?: return null

    return if (suggestedFrequency != selectedFrequency) {
        "Warning: the medication instructions look like ${suggestedFrequency.name.lowercase().replace('_', ' ')}, but the schedule is set to ${selectedFrequency.name.lowercase().replace('_', ' ')}."
    } else {
        null
    }
}

private fun sameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun firstTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.ONCE_DAILY -> "Daily Reminder Time"
        DoseFrequency.TWICE_DAILY -> "Morning Time"
        DoseFrequency.THREE_TIMES_DAILY -> "Morning Time"
        DoseFrequency.WEEKLY -> "Weekly Reminder Time"
        DoseFrequency.AS_NEEDED -> "Reminder Time"
    }
}

private fun secondTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.TWICE_DAILY -> "Evening Time"
        DoseFrequency.THREE_TIMES_DAILY -> "Afternoon Time"
        else -> "Second Reminder Time"
    }
}

private fun thirdTimeLabel(frequency: DoseFrequency): String {
    return when (frequency) {
        DoseFrequency.THREE_TIMES_DAILY -> "Night Time"
        else -> "Third Reminder Time"
    }
}
