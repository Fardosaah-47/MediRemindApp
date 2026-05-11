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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import com.example.mediremind.ui.theme.MediRemindTheme

private val HomeCream = Color(0xFFF5F1FF)
private val HomeCard = Color(0xFFFFFFFF)
private val HomeInk = Color(0xFF2F2854)
private val HomeMuted = Color(0xFF7E75A3)
private val HomeBrown = Color(0xFFCFC3FF)
private val HomeBrownDark = Color(0xFF6D57D9)
private val HomeNavy = Color(0xFF1F2850)
private val HomePill = Color(0xFFEDE7FF)
private val HomeMedical = Color(0xFF7B61FF)
private val HomeSuccess = Color(0xFF2F8F6B)
private val HomePink = Color(0xFFF06BA3)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    patientName: String? = null,
    medicationCount: Int = 0,
    scheduleCount: Int = 0,
    dueTodayCount: Int = 0,
    loggedTodayCount: Int = 0,
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
    val remainingDoses = (dueTodayCount - loggedTodayCount).coerceAtLeast(0)
    val nextMedicineTitle = when {
        !patientName.isNullOrBlank() && hasDoseToday -> "Next medicine"
        !patientName.isNullOrBlank() -> "No dose due"
        else -> "Set up first"
    }
    val nextMedicineName = when {
        !patientName.isNullOrBlank() && hasDoseToday -> "Open Dose Log"
        !patientName.isNullOrBlank() -> "All clear"
        else -> "Create profile"
    }
    val nextMedicineTime = when {
        !patientName.isNullOrBlank() && hasDoseToday -> "$remainingDoses left"
        !patientName.isNullOrBlank() -> "Today"
        else -> "Start"
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
                    onProfileClick = onStartProfileFlow
                )
            }

            item {
                NextDoseHero(
                    title = nextMedicineTitle,
                    medicineName = nextMedicineName,
                    timeLabel = nextMedicineTime,
                    actionLabel = if (patientName.isNullOrBlank()) "Profile" else "Log dose",
                    actionIcon = if (patientName.isNullOrBlank()) Icons.Outlined.AccountCircle else Icons.Outlined.CheckCircle,
                    onActionClick = if (patientName.isNullOrBlank()) onStartProfileFlow else onStartDoseLoggingFlow
                )
            }

            item {
                TodayScheduleSection(
                    dueTodayCount = dueTodayCount,
                    loggedTodayCount = loggedTodayCount,
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
            onDoseClick = onStartDoseLoggingFlow,
            onReportClick = onStartPatientReportFlow,
            onMedicationClick = onStartMedicationFlow
        )
    }
}

@Composable
private fun HomeTopBar(
    patientName: String,
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

        Surface(
            onClick = onProfileClick,
            shape = CircleShape,
            color = HomeCard,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile",
                    tint = HomeBrownDark,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NextDoseHero(
    title: String,
    medicineName: String,
    timeLabel: String,
    actionLabel: String,
    actionIcon: ImageVector,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HomeBrown),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(178.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD9D0FF), Color(0xFF9E8CF2))
                    )
                )
                .padding(24.dp)
        ) {
            MedicineIllustration(
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.94f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = medicineName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF3E5)
                    )
                )
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionLabel,
                            tint = HomeBrownDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = HomeBrownDark
                            )
                        )
                    }
                }
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
                .background(Color(0xFFFFF7EC))
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
                .background(Color(0xFFFFF7EC))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD8B45E))
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
private fun TodayScheduleSection(
    dueTodayCount: Int,
    loggedTodayCount: Int,
    onDoseLogClick: () -> Unit
) {
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
                text = if (dueTodayCount > 0) "$dueTodayCount due" else "No due",
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
            DosePreviewCard(
                title = "Remaining",
                subtitle = "${(dueTodayCount - loggedTodayCount).coerceAtLeast(0)} left today",
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
                detail = "$scheduleCount times",
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
            containerColor = if (highlight) Color(0xFFFFF7EC) else HomeCard
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
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
                    .background(Color(0xFFEFE0D3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = HomeMedical,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
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
    onDoseClick: () -> Unit,
    onReportClick: () -> Unit,
    onMedicationClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 12.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            shape = RoundedCornerShape(30.dp),
            color = HomeCard,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
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
                    onClick = onDoseClick,
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
        modifier = modifier.height(68.dp),
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
                    .clip(RoundedCornerShape(16.dp))
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
        modifier = modifier.height(70.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(HomeNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Dose Log",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "Dose",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HomeNavy,
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
            nextStepLabel = "You have 2 doses remaining today. Tap Dose Log to record them."
        )
    }
}
