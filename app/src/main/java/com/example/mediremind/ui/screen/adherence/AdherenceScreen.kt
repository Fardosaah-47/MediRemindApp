package com.example.mediremind.ui.screen.adherence

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediremind.data.model.DoseLog
import com.example.mediremind.data.model.DoseStatus
import com.example.mediremind.domain.AdherenceEngine
import com.example.mediremind.domain.DayAdherence
import com.example.mediremind.domain.StreakResult
import com.example.mediremind.domain.WeeklySummary
import com.example.mediremind.ui.components.MediRemindTopBar
import com.example.mediremind.ui.theme.DangerLight
import com.example.mediremind.ui.theme.DangerRed
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediMuted
import com.example.mediremind.ui.theme.MediPrimary
import com.example.mediremind.ui.theme.MediPrimaryDark
import com.example.mediremind.ui.theme.MediPrimaryLight
import com.example.mediremind.ui.theme.MediRemindTheme
import com.example.mediremind.ui.theme.MediSurfaceRaised
import com.example.mediremind.ui.theme.SuccessGreen
import com.example.mediremind.ui.theme.SuccessLight
import com.example.mediremind.ui.theme.WarningLight
import com.example.mediremind.ui.theme.WarningOrange

@Composable
fun AdherenceScreen(
    modifier: Modifier = Modifier,
    logs: List<DoseLog> = emptyList(),
    patientName: String = "Patient",
    onBackClick: () -> Unit = {}
) {
    val streak = remember(logs) { AdherenceEngine.computeStreak(logs) }
    val weekly = remember(logs) { AdherenceEngine.computeWeeklySummary(logs) }
    val recent = remember(logs) { AdherenceEngine.computeRecentDays(logs, numDays = 28) }

    Scaffold(
        topBar = {
            MediRemindTopBar(title = "Adherence", onBackClick = onBackClick)
        },
        containerColor = MediCream
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = patientName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MediMuted
                )
            )
            StreakCard(streak)
            WeeklyCard(weekly)
            RecentDaysCard(recent)
            LegendCard()
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun StreakCard(streak: StreakResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MediSurfaceRaised),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Your streak",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MediInk
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StreakStat(
                    label = "Current",
                    value = "${streak.currentStreak}",
                    detail = "days",
                    color = DangerRed,
                    icon = Icons.Outlined.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                StreakStat(
                    label = "Best",
                    value = "${streak.longestStreak}",
                    detail = "days",
                    color = WarningOrange,
                    icon = Icons.Outlined.Schedule,
                    modifier = Modifier.weight(1f)
                )
                StreakStat(
                    label = "Rate",
                    value = "${streak.overallRatePercent}%",
                    detail = "taken",
                    color = SuccessGreen,
                    icon = Icons.Outlined.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MediPrimaryLight
            ) {
                Text(
                    text = when {
                        streak.currentStreak >= 7 -> "Strong routine. Keep every scheduled dose covered."
                        streak.currentStreak >= 3 -> "Good rhythm. One full day at a time."
                        streak.currentStreak >= 1 -> "Streak started. Complete today to keep it going."
                        else -> "Take every scheduled dose today to start a streak."
                    },
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MediPrimaryDark
                    )
                )
            }
        }
    }
}

@Composable
private fun StreakStat(
    label: String,
    value: String,
    detail: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MediInk
                ),
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MediInk
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall.copy(color = MediMuted),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeeklyCard(weekly: WeeklySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MediSurfaceRaised),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "This week",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MediInk
                        )
                    )
                    Text(
                        text = "${weekly.weekTaken} of ${weekly.weekScheduled} doses taken",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MediMuted)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = rateColor(weekly.weekRatePercent / 100f).copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "${weekly.weekRatePercent}%",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = rateColor(weekly.weekRatePercent / 100f)
                        )
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weekly.days.forEach { day ->
                    DayBar(day = day, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayBar(day: DayAdherence, modifier: Modifier = Modifier) {
    val barColor = rateColor(day.rate)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = if (day.scheduled == 0) "-" else "${(day.rate * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MediMuted
            ),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(50))
                .background(MediPrimaryLight.copy(alpha = 0.35f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((22f + 98f * day.rate).coerceAtMost(120f)).dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (day.scheduled == 0) MediPrimaryLight else barColor)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MediMuted
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun RecentDaysCard(recent: List<DayAdherence>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MediSurfaceRaised),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Last 28 days",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MediInk
                )
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recent.forEach { day ->
                    RecentDayDot(day)
                }
            }
        }
    }
}

@Composable
private fun RecentDayDot(day: DayAdherence) {
    val color = rateColor(day.rate)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (day.scheduled == 0) MediPrimaryLight else color),
            contentAlignment = Alignment.Center
        ) {
            if (day.scheduled > 0 && day.rate >= 1f) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Text(
            text = day.date.takeLast(2),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MediMuted
            )
        )
    }
}

@Composable
private fun LegendCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MediSurfaceRaised
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = SuccessGreen, label = "Good")
            LegendItem(color = WarningOrange, label = "Partial")
            LegendItem(color = DangerRed, label = "Missed")
            LegendItem(color = MediPrimary, label = "No data")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = MediMuted),
            maxLines = 1
        )
    }
}

private fun rateColor(rate: Float): Color {
    return when {
        rate >= 0.8f -> SuccessGreen
        rate >= 0.5f -> WarningOrange
        rate > 0f -> DangerRed
        else -> MediPrimary
    }
}

@Preview(showBackground = true)
@Composable
private fun AdherenceScreenPreview() {
    MediRemindTheme {
        AdherenceScreen(
            patientName = "Mary Achieng",
            logs = listOf(
                DoseLog(patientId = 1, doseScheduleId = 1, medicationId = 1, scheduledTime = "08:00 AM", logDate = "2026-05-14", status = DoseStatus.TAKEN),
                DoseLog(patientId = 1, doseScheduleId = 2, medicationId = 1, scheduledTime = "08:00 PM", logDate = "2026-05-14", status = DoseStatus.TAKEN),
                DoseLog(patientId = 1, doseScheduleId = 1, medicationId = 1, scheduledTime = "08:00 AM", logDate = "2026-05-13", status = DoseStatus.TAKEN),
                DoseLog(patientId = 1, doseScheduleId = 2, medicationId = 1, scheduledTime = "08:00 PM", logDate = "2026-05-13", status = DoseStatus.MISSED)
            )
        )
    }
}
