package com.example.mediremind.ui.screen.home

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediremind.ui.theme.MediAmber
import com.example.mediremind.ui.theme.MediBorder
import com.example.mediremind.ui.theme.MediCoral
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediGreen
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediMuted
import com.example.mediremind.ui.theme.MediPrimary
import com.example.mediremind.ui.theme.MediPrimaryDark
import com.example.mediremind.ui.theme.MediPrimaryLight
import com.example.mediremind.ui.theme.MediSurfaceRaised
import com.example.mediremind.ui.theme.MediTeal
import com.example.mediremind.ui.theme.MediRemindTheme

private val HomeCream = MediCream
private val HomeCard = MediSurfaceRaised
private val HomeInk = MediInk
private val HomeMuted = MediMuted
private val HomeBrown = MediPrimary
private val HomeBrownDark = MediPrimaryDark
private val HomePill = MediPrimaryLight
private val HomeMedical = MediTeal
private val HomeSuccess = MediGreen
private val HomePink = MediAmber
private val HomeWarning = MediCoral

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    patientName: String? = null,
    medicationCount: Int = 0,
    scheduleCount: Int = 0,
    dueTodayCount: Int = 0,
    loggedTodayCount: Int = 0,
    takenTodayCount: Int = 0,
    missedTodayCount: Int = 0,
    nextStepLabel: String = "Set up the patient profile first.",
    onStartMedicationFlow: () -> Unit = {},
    onStartScheduleFlow: () -> Unit = {},
    onStartDoseLoggingFlow: () -> Unit = {},
    onStartQrImportFlow: () -> Unit = {},
    onStartProfileFlow: () -> Unit = {},
    onStartPatientReportFlow: () -> Unit = {},
    onStartCaregiverScanFlow: () -> Unit = {}
) {
    val displayName = patientName?.takeIf { it.isNotBlank() } ?: "Patient"
    val hasDoseToday = dueTodayCount > 0
    val pendingTodayCount = (dueTodayCount - loggedTodayCount - missedTodayCount).coerceAtLeast(0)
    val dayProgressPercent = if (dueTodayCount > 0) {
        ((takenTodayCount.coerceAtMost(dueTodayCount).toFloat() / dueTodayCount.toFloat()) * 100).toInt()
    } else {
        0
    }
    val streakLabel = if (dueTodayCount > 0 && pendingTodayCount == 0 && missedTodayCount == 0) {
        "1 day"
    } else {
        "0 days"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeCream)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 138.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeTopBar(
                    patientName = displayName,
                    onSearchClick = onStartMedicationFlow,
                    onAlarmClick = onStartScheduleFlow,
                    onProfileClick = onStartProfileFlow
                )
            }

            item {
                DailyProgressHero(
                    progressPercent = dayProgressPercent,
                    takenTodayCount = takenTodayCount,
                    dueTodayCount = dueTodayCount,
                    pendingTodayCount = pendingTodayCount,
                    missedTodayCount = missedTodayCount,
                    streakLabel = streakLabel
                )
            }

            item {
                TodayScheduleSection(
                    dueTodayCount = dueTodayCount,
                    loggedTodayCount = loggedTodayCount,
                    missedTodayCount = missedTodayCount,
                    onDoseLogClick = onStartDoseLoggingFlow
                )
            }

            item {
                SetupSection(
                    medicationCount = medicationCount,
                    scheduleCount = scheduleCount,
                    onStartProfileFlow = onStartProfileFlow,
                    onStartMedicationFlow = onStartMedicationFlow,
                    onStartScheduleFlow = onStartScheduleFlow,
                    onStartQrImportFlow = onStartQrImportFlow
                )
            }

            item {
                CaregiverSection(
                    nextStepLabel = nextStepLabel,
                    onStartPatientReportFlow = onStartPatientReportFlow,
                    onStartCaregiverScanFlow = onStartCaregiverScanFlow
                )
            }
        }

        HomeBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = {},
            onQrClick = onStartQrImportFlow,
            onAddClick = onStartMedicationFlow,
            onReportClick = onStartPatientReportFlow,
            onMedicationClick = onStartMedicationFlow
        )
    }
}

@Composable
private fun HomeTopBar(
    patientName: String,
    onSearchClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = patientName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeInk
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = HomeMuted
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TopShortcutButton(
                icon = Icons.Outlined.Search,
                contentDescription = "Search medicines",
                onClick = onSearchClick
            )
            TopShortcutButton(
                icon = Icons.Outlined.Notifications,
                contentDescription = "Alarms",
                onClick = onAlarmClick
            )
            TopShortcutButton(
                icon = Icons.Outlined.AccountCircle,
                contentDescription = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun TopShortcutButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = HomeCard,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = HomeBrownDark,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun DailyProgressHero(
    progressPercent: Int,
    takenTodayCount: Int,
    dueTodayCount: Int,
    pendingTodayCount: Int,
    missedTodayCount: Int,
    streakLabel: String
) {
    val progressColor = HomeSuccess
    val progressFraction = (progressPercent / 100f).coerceIn(0f, 1f)
    val statusLabel = when {
        dueTodayCount == 0 -> "No doses today"
        takenTodayCount > 0 -> "$takenTodayCount taken"
        missedTodayCount > 0 -> "$missedTodayCount missed dose"
        pendingTodayCount == 0 -> "All done"
        else -> "$pendingTodayCount pending"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HomeBrownDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(244.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFA78BFA), Color(0xFFC4B5FD))
                    )
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 54.dp, y = (-72).dp)
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-52).dp, y = 46.dp)
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.10f))
            )

            ProgressMedicineArt(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-28).dp, y = 2.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 0.dp, y = 44.dp)
                    .size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(144.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(118.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        CircularProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.size(126.dp),
                            color = progressColor,
                            trackColor = Color.White.copy(alpha = 0.34f),
                            strokeWidth = 9.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "taken",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                missedTodayCount > 0 -> HomeWarning
                                pendingTodayCount > 0 -> HomePink
                                else -> HomeSuccess
                            }
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (pendingTodayCount == 0 && missedTodayCount == 0 && dueTodayCount > 0) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = when {
                                missedTodayCount > 0 -> missedTodayCount.toString()
                                pendingTodayCount > 0 -> pendingTodayCount.toString()
                                else -> "0"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(end = 168.dp)
            ) {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.84f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$takenTodayCount of $dueTodayCount taken",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.86f)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressMiniChip(
                    label = "Left",
                    value = pendingTodayCount.toString(),
                    color = HomeBrownDark,
                    icon = Icons.Outlined.Schedule
                )
                ProgressMiniChip(
                    label = "Streak",
                    value = streakLabel,
                    color = HomePink,
                    icon = Icons.Outlined.LocalFireDepartment
                )
            }
        }
    }
}

@Composable
private fun ProgressMiniChip(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = HomeInk
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HomeInk
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MedicineIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 124.dp, height = 116.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 78.dp, height = 34.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFF3DC))
        )
        Box(
            modifier = Modifier
                .size(width = 39.dp, height = 34.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp))
                    .background(HomePink)
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(Color(0xFFFFF3DC))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(HomePink)
            )
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                    .background(HomePink.copy(alpha = 0.78f))
        )
    }
}

@Composable
private fun ProgressMedicineArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 154.dp, height = 92.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = 76.dp, height = 30.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFF7EC))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-38).dp)
                .size(width = 38.dp, height = 30.dp)
                .clip(RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp))
                .background(HomeWarning)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-18).dp, y = (-2).dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF7EC))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(HomePink)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-28).dp, y = 10.dp)
                .size(13.dp)
                .clip(CircleShape)
                .background(HomeWarning.copy(alpha = 0.78f))
        )
    }
}

@Composable
private fun TodayScheduleSection(
    dueTodayCount: Int,
    loggedTodayCount: Int,
    missedTodayCount: Int,
    onDoseLogClick: () -> Unit
) {
    val pendingTodayCount = (dueTodayCount - loggedTodayCount - missedTodayCount).coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today schedule",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeInk
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (dueTodayCount > 0) "$pendingTodayCount left" else "No due",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HomeMuted
                )
            )
        }

        if (dueTodayCount == 0) {
            DosePreviewCard(
                title = "No dose now",
                subtitle = "Add or scan medicines",
                trailing = "Setup",
                icon = Icons.Outlined.MedicalServices,
                iconTint = HomeMedical,
                onClick = onDoseLogClick
            )
        } else {
            DosePreviewCard(
                title = "Logged",
                subtitle = "$loggedTodayCount of $dueTodayCount done",
                trailing = if (loggedTodayCount >= dueTodayCount) "Done" else "Open",
                icon = Icons.Outlined.Check,
                iconTint = HomeSuccess,
                onClick = onDoseLogClick
            )
            if (missedTodayCount > 0) {
                DosePreviewCard(
                    title = "Missed",
                    subtitle = "$missedTodayCount closed today",
                    trailing = "Review",
                    icon = Icons.Outlined.Close,
                    iconTint = HomeWarning,
                    onClick = onDoseLogClick
                )
            }
            DosePreviewCard(
                title = "Remaining",
                subtitle = "$pendingTodayCount left today",
                trailing = "Dose Log",
                icon = Icons.Outlined.Schedule,
                iconTint = HomeMedical,
                onClick = onDoseLogClick
            )
        }
    }
}

@Composable
private fun DosePreviewCard(
    title: String,
    subtitle: String,
    trailing: String,
    icon: ImageVector,
    iconTint: Color = HomeBrownDark,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = HomeCard),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HomePill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HomeInk
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = HomeMuted
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = HomeBrownDark
                )
            )
        }
    }
}

@Composable
private fun SetupSection(
    medicationCount: Int,
    scheduleCount: Int,
    onStartProfileFlow: () -> Unit,
    onStartMedicationFlow: () -> Unit,
    onStartScheduleFlow: () -> Unit,
    onStartQrImportFlow: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Setup",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = HomeInk
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SetupActionCard(
                label = "Profile",
                detail = "Patient",
                icon = Icons.Outlined.AccountCircle,
                onClick = onStartProfileFlow,
                modifier = Modifier.weight(1f)
            )
            SetupActionCard(
                label = "Medicines",
                detail = "$medicationCount saved",
                icon = Icons.Outlined.LocalPharmacy,
                onClick = onStartMedicationFlow,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SetupActionCard(
                label = "Schedules",
                detail = if (scheduleCount == 1) "1 medicine" else "$scheduleCount medicines",
                icon = Icons.Outlined.CalendarToday,
                onClick = onStartScheduleFlow,
                modifier = Modifier.weight(1f)
            )
            SetupActionCard(
                label = "QR",
                detail = "Scan meds",
                icon = Icons.Outlined.QrCodeScanner,
                onClick = onStartQrImportFlow,
                modifier = Modifier.weight(1f),
                highlight = true
            )
        }
    }
}

@Composable
private fun SetupActionCard(
    label: String,
    detail: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (highlight) Color(0xFFFFF5E3) else HomeCard
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-26).dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background((if (highlight) HomePink else HomeMedical).copy(alpha = 0.12f))
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (highlight) Color(0xFFFFE2AD) else HomePill),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (highlight) HomeInk else HomeMedical,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = HomeInk
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall.copy(color = HomeMuted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CaregiverSection(
    nextStepLabel: String,
    onStartPatientReportFlow: () -> Unit,
    onStartCaregiverScanFlow: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Caregiver",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = HomeInk
            )
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SetupActionCard(
                label = "Report",
                detail = "7 days",
                icon = Icons.AutoMirrored.Outlined.Assignment,
                onClick = onStartPatientReportFlow,
                modifier = Modifier.weight(1f)
            )
            SetupActionCard(
                label = "Scan",
                detail = "Caregiver",
                icon = Icons.Outlined.CameraAlt,
                onClick = onStartCaregiverScanFlow,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = nextStepLabel,
            style = MaterialTheme.typography.bodySmall.copy(
                color = HomeMuted,
                lineHeight = 18.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeBottomBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onQrClick: () -> Unit,
    onAddClick: () -> Unit,
    onReportClick: () -> Unit,
    onMedicationClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HomeCard)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MediBorder)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(0.dp),
            color = HomeCard,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Outlined.Home,
                    selected = true,
                    onClick = onHomeClick,
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    label = "QR",
                    icon = Icons.Outlined.QrCodeScanner,
                    selected = false,
                    onClick = onQrClick,
                    modifier = Modifier.weight(1f)
                )
                DoseNavItem(
                    onClick = onAddClick,
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    label = "Report",
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    selected = false,
                    onClick = onReportClick,
                    modifier = Modifier.weight(1f)
                )
                BottomNavItem(
                    label = "Meds",
                    icon = Icons.Outlined.LocalPharmacy,
                    selected = false,
                    onClick = onMedicationClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (selected) HomePill else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) HomeBrownDark else HomeMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) HomeInk else HomeMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DoseNavItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(66.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HomeBrownDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add medicine",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "Add",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HomeInk,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MediRemindTheme {
        HomeScreen(
            patientName = "Mary Achieng",
            medicationCount = 3,
            scheduleCount = 2,
            dueTodayCount = 4,
            loggedTodayCount = 2,
            takenTodayCount = 2,
            nextStepLabel = "You have 2 doses remaining today. Tap Dose Log to record them."
        )
    }
}
