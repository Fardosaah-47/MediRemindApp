package com.example.mediremind.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseFrequency
import com.example.mediremind.data.model.DoseSchedule
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.components.SectionLabel
import com.example.mediremind.ui.components.StatusChip
import com.example.mediremind.ui.theme.MediRemindTheme

data class ScheduleTimeDisplayItem(
    val schedule: DoseSchedule,
    val reminderTime: String
)

data class ScheduleDisplayGroup(
    val medicationName: String,
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
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
        containerColor = MaterialTheme.colorScheme.background
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
                        text = "${scheduleGroups.size} medication schedule group${if (scheduleGroups.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = group.medicationName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    label = group.frequency.name.lowercase().replace('_', ' ')
                )
                Text(
                    text = group.periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SectionLabel(text = "REMINDER TIMES")
            Spacer(modifier = Modifier.height(8.dp))

            group.timeEntries.forEachIndexed { index, entry ->
                ScheduleTimeRow(
                    reminderTime = entry.reminderTime,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reminderTime,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(14.dp)
            )
        }
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No schedules yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Add a schedule so MediRemind knows when each medication should be taken.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

private fun sampleScheduleDisplayGroups(): List<ScheduleDisplayGroup> {
    return listOf(
        ScheduleDisplayGroup(
            medicationName = "Paracetamol",
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
