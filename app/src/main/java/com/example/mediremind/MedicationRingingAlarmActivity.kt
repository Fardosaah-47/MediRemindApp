package com.example.mediremind

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediremind.domain.EXTRA_ALARM_PATIENT_ID
import com.example.mediremind.domain.EXTRA_END_DATE
import com.example.mediremind.domain.EXTRA_FREQUENCY
import com.example.mediremind.domain.EXTRA_MEDICATION_NAME
import com.example.mediremind.domain.EXTRA_OPEN_DOSE_LOG
import com.example.mediremind.domain.EXTRA_PATIENT_NAME
import com.example.mediremind.domain.EXTRA_SCHEDULED_TIME
import com.example.mediremind.domain.EXTRA_SCHEDULE_ID
import com.example.mediremind.domain.EXTRA_START_DATE
import com.example.mediremind.domain.MedicationAlarmScheduler
import com.example.mediremind.ui.theme.MediRemindTheme

class MedicationRingingAlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val alarmDetails = AlarmDetails.from(intent)
        startAlarmSound()

        setContent {
            MediRemindTheme {
                RingingAlarmScreen(
                    alarmDetails = alarmDetails,
                    onStop = {
                        stopAlarmSound()
                        finish()
                    },
                    onOpenDoseLog = {
                        stopAlarmSound()
                        openDoseLog(alarmDetails.patientId)
                    },
                    onSnooze = {
                        stopAlarmSound()
                        MedicationAlarmScheduler.scheduleSnoozeAlarm(
                            context = applicationContext,
                            scheduleId = alarmDetails.scheduleId,
                            patientId = alarmDetails.patientId,
                            patientName = alarmDetails.patientName,
                            medicationName = alarmDetails.medicationName,
                            timeString = alarmDetails.scheduledTime,
                            frequencyName = alarmDetails.frequency,
                            startDate = alarmDetails.startDate,
                            endDate = alarmDetails.endDate
                        )
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }

    private fun showOverLockScreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    private fun startAlarmSound() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, alarmUri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
            }
            play()
        }

        vibrator = getDeviceVibrator()
        val pattern = longArrayOf(0, 700, 350, 700, 900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
    }

    private fun getDeviceVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun openDoseLog(patientId: Long) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_DOSE_LOG, true)
                putExtra(EXTRA_ALARM_PATIENT_ID, patientId)
            }
        )
        finish()
    }
}

private data class AlarmDetails(
    val scheduleId: Long,
    val patientId: Long,
    val patientName: String,
    val medicationName: String,
    val scheduledTime: String,
    val frequency: String,
    val startDate: String,
    val endDate: String
) {
    companion object {
        fun from(intent: Intent): AlarmDetails {
            return AlarmDetails(
                scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, 0L),
                patientId = intent.getLongExtra(EXTRA_ALARM_PATIENT_ID, 0L),
                patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Patient",
                medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Medication",
                scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME)
                    ?.takeIf { it.isNotBlank() }
                    ?: "now",
                frequency = intent.getStringExtra(EXTRA_FREQUENCY).orEmpty(),
                startDate = intent.getStringExtra(EXTRA_START_DATE).orEmpty(),
                endDate = intent.getStringExtra(EXTRA_END_DATE).orEmpty()
            )
        }
    }
}

@Composable
private fun RingingAlarmScreen(
    alarmDetails: AlarmDetails,
    onStop: () -> Unit,
    onOpenDoseLog: () -> Unit,
    onSnooze: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F9FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF2F80ED),
                        modifier = Modifier
                            .background(Color(0xFFEAF3FF), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Medicine alarm",
                        color = Color(0xFF222222),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = alarmDetails.patientName,
                        color = Color(0xFF2F80ED),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = alarmDetails.medicationName,
                        color = Color(0xFF222222),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scheduled for ${alarmDetails.scheduledTime}",
                        color = Color(0xFF555555),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onOpenDoseLog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Text(
                            text = "Open Dose Log",
                            modifier = Modifier.padding(start = 10.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSnooze,
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Snooze,
                                contentDescription = null
                            )
                            Text(
                                text = "Snooze",
                                modifier = Modifier.padding(start = 6.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        OutlinedButton(
                            onClick = onStop,
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Stop",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
