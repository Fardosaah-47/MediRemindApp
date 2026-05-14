package com.example.mediremind.ui.screen.schedule

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.data.model.MedicationForm
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.theme.MediAmber
import com.example.mediremind.ui.theme.MediBlue
import com.example.mediremind.ui.theme.MediCoral
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediMuted
import com.example.mediremind.ui.theme.MediPurple
import com.example.mediremind.ui.theme.MediRemindTheme
import com.example.mediremind.ui.theme.MediSurfaceRaised
import com.example.mediremind.ui.theme.MediTeal

data class ScheduleTimeDisplayItem(
    val schedule: DoseSchedule,
    val reminderTime: String
)

data class ScheduleDisplayGroup(
    val medicationName: String,
    val medicationForm: MedicationForm = MedicationForm.OTHER,
    val frequency: DoseFrequency,
    val periodLabel: String,
    val timeEntries: List<ScheduleTimeDisplayItem>
)

@Composable
fun ScheduleListScreen(
    modifier: Modifier = Modifier,
    scheduleGroups: List<ScheduleDisplayGroup> = sampleScheduleDisplayGroups(),
    onAddScheduleClick: () -> Unit = {},
    onScheduleClick: (DoseSchedule) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            MediRemindTopBar(title = "Dose Schedules", onBackClick = onBackClick)
        },
        floatingActionButton = {
            Button(
                onClick = onAddScheduleClick,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAmber,
                    contentColor = MediInk
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddAlarm,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                Text("Add Schedule", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = MediCream
    ) { innerPadding ->
        if (scheduleGroups.isEmpty()) {
            EmptySchedulesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "SCHEDULES - ${scheduleGroups.size} MEDICATION GROUP${if (scheduleGroups.size != 1) "S" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MediTeal
                    )
                }

                items(scheduleGroups) { group ->
                    ScheduleGroupCard(
                        group = group,
                        onScheduleClick = onScheduleClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleGroupCard(
    group: ScheduleDisplayGroup,
    onScheduleClick: (DoseSchedule) -> Unit
) {
    val formColor = formColor(group.medicationForm)
    val formLabel = group.medicationForm.name.lowercase().replaceFirstChar { it.uppercase() }
    val initials = group.medicationName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "Rx" }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MediSurfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(formColor.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 24.dp, y = (-28).dp)
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(formColor.copy(alpha = 0.14f))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-24).dp, y = 30.dp)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MediAmber.copy(alpha = 0.14f))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SchedulePill(
                        label = formLabel,
                        containerColor = Color.White.copy(alpha = 0.84f),
                        contentColor = formColor
                    )
                    SchedulePill(
                        label = "${group.timeEntries.size} time${if (group.timeEntries.size == 1) "" else "s"}",
                        containerColor = Color.White.copy(alpha = 0.72f),
                        contentColor = MediInk
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MediAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MediInk
                        )
                    }
                    Column {
                        Text(
                            text = group.medicationName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MediInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = group.periodLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MediMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SchedulePill(
                    label = group.frequency.name.lowercase().replace('_', ' '),
                    containerColor = formColor.copy(alpha = 0.12f),
                    contentColor = formColor
                )
                Text(
                    text = "Reminder times",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MediMuted
                )
            }

            group.timeEntries.forEachIndexed { index, entry ->
                ScheduleTimeRow(
                    reminderTime = entry.reminderTime,
                    accentColor = formColor,
                    onClick = { onScheduleClick(entry.schedule) }
                )
                if (index != group.timeEntries.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimeRow(
    reminderTime: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MediCream
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Text(
                    text = reminderTime,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MediInk
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MediMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SchedulePill(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    androidx.compose.material3.Surface(
        color = containerColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptySchedulesState(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MediTeal.copy(alpha = 0.10f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MediTeal,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No schedules yet",
            style = MaterialTheme.typography.titleMedium,
                color = MediInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add a schedule so MediRemind knows when each medication should be taken.",
            style = MaterialTheme.typography.bodyMedium,
                color = MediMuted,
            textAlign = TextAlign.Center
        )
    }
}

private fun formColor(form: MedicationForm): Color {
    return when (form) {
        MedicationForm.TABLET -> MediTeal
        MedicationForm.CAPSULE -> MediPurple
        MedicationForm.LIQUID -> MediBlue
        MedicationForm.INJECTION -> MediCoral
        MedicationForm.OTHER -> MediMuted
    }
}

private fun sampleScheduleDisplayGroups(): List<ScheduleDisplayGroup> {
    return listOf(
        ScheduleDisplayGroup(
            medicationName = "Paracetamol",
            medicationForm = MedicationForm.TABLET,
            frequency = DoseFrequency.THREE_TIMES_DAILY,
            periodLabel = "19 Apr 2026 - 29 Apr 2026",
            timeEntries = listOf(
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 1, medicationId = 1, time = "09:00 AM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "09:00 AM"
                ),
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 2, medicationId = 1, time = "01:00 PM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "01:00 PM"
                ),
                ScheduleTimeDisplayItem(
                    schedule = DoseSchedule(id = 3, medicationId = 1, time = "09:00 PM", frequency = DoseFrequency.THREE_TIMES_DAILY),
                    reminderTime = "09:00 PM"
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleListScreenPreview() {
    MediRemindTheme {
        ScheduleListScreen()
    }
}
