package com.example.mediremind.domain

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediremind.MainActivity
import com.example.mediremind.MedicationRingingAlarmActivity
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.DoseFrequency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, 0L)
        val patientId = intent.getLongExtra(EXTRA_ALARM_PATIENT_ID, 0L)
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: "Patient"
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
        val frequency = intent.getStringExtra(EXTRA_FREQUENCY) ?: DoseFrequency.ONCE_DAILY.name
        val startDate = intent.getStringExtra(EXTRA_START_DATE).orEmpty()
        val endDate = intent.getStringExtra(EXTRA_END_DATE).orEmpty()

        createNotificationChannel(context)
        triggerVibration(context)

        val tapIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_DOSE_LOG, true)
                putExtra(EXTRA_ALARM_PATIENT_ID, patientId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ringingIntent = PendingIntent.getActivity(
            context,
            ringingRequestCode(scheduleId),
            Intent(context, MedicationRingingAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(EXTRA_ALARM_PATIENT_ID, patientId)
                putExtra(EXTRA_PATIENT_NAME, patientName)
                putExtra(EXTRA_MEDICATION_NAME, medicationName)
                putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
                putExtra(EXTRA_FREQUENCY, frequency)
                putExtra(EXTRA_START_DATE, startDate)
                putExtra(EXTRA_END_DATE, endDate)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            NotificationManagerCompat.from(context).notify(
                scheduleId.toInt(),
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("$patientName: time to take $medicationName")
                    .setContentText("Scheduled for $scheduledTime - tap to log your dose")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                "$patientName, your $medicationName dose is due now.\n" +
                                    "Scheduled time: $scheduledTime\n\n" +
                                    "Tap to open MediRemind and log this dose."
                            )
                    )
                    .setContentIntent(tapIntent)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    .setVibrate(VIBRATION_PATTERN)
                    .setFullScreenIntent(ringingIntent, true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .addAction(
                        android.R.drawable.ic_menu_agenda,
                        "Log dose",
                        tapIntent
                    )
                    .build()
            )
        }

        if (scheduleId != 0L && scheduledTime.isNotBlank()) {
            MedicationAlarmScheduler.scheduleAlarm(
                context = context,
                scheduleId = scheduleId,
                patientId = patientId,
                patientName = patientName,
                medicationName = medicationName,
                timeString = scheduledTime,
                frequencyName = frequency,
                startDate = startDate,
                endDate = endDate
            )
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Dose reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Medication dose reminder alerts"
            setSound(alarmSound, audioAttributes)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            setShowBadge(true)
            enableLights(true)
            lightColor = 0xFFA78BFA.toInt()
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun triggerVibration(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(VIBRATION_PATTERN, -1)
            )
            return
        }

        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATION_PATTERN, -1)
        }
    }
}

class MedicationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabaseProvider.getDatabase(context)
                val medications = database.medicationDao().getAllMedications()
                val patientNameById = database.userProfileDao()
                    .getAllUserProfiles()
                    .associate { profile ->
                        profile.id to profile.fullName.ifBlank { "Patient" }
                    }
                val medicationNameById = medications.associate { medication ->
                    medication.id to medication.name
                }
                val medicationPatientIdById = medications.associate { medication ->
                    medication.id to medication.patientId
                }
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                database.doseScheduleDao()
                    .getAllDoseSchedules()
                    .filter { schedule ->
                        schedule.frequency != DoseFrequency.AS_NEEDED &&
                            (schedule.endDate.isBlank() || schedule.endDate >= todayDate)
                    }
                    .forEach { schedule ->
                        MedicationAlarmScheduler.scheduleAlarm(
                            context = context,
                            scheduleId = schedule.id,
                            patientId = schedule.patientId.takeIf { it != 0L }
                                ?: medicationPatientIdById[schedule.medicationId]
                                ?: 0L,
                            patientName = patientNameById[schedule.patientId] ?: "Patient",
                            medicationName = medicationNameById[schedule.medicationId] ?: "Medication",
                            timeString = schedule.time,
                            frequencyName = schedule.frequency.name,
                            startDate = schedule.startDate,
                            endDate = schedule.endDate
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

object MedicationAlarmScheduler {
    fun scheduleAlarm(
        context: Context,
        scheduleId: Long,
        patientId: Long,
        patientName: String,
        medicationName: String,
        timeString: String,
        frequencyName: String,
        startDate: String,
        endDate: String
    ) {
        if (scheduleId == 0L || timeString.isBlank()) return
        if (frequencyName == DoseFrequency.AS_NEEDED.name) return

        val triggerTime = parseTimeToNextTrigger(
            timeString = timeString,
            frequencyName = frequencyName,
            startDate = startDate,
            endDate = endDate
        ) ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(
            context = context,
            scheduleId = scheduleId,
            patientId = patientId,
            patientName = patientName,
            medicationName = medicationName,
            timeString = timeString,
            frequencyName = frequencyName,
            startDate = startDate,
            endDate = endDate,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            Intent(context, MedicationAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        cancelDoseNotification(context, scheduleId)
    }

    fun cancelDoseNotification(context: Context, scheduleId: Long) {
        if (scheduleId == 0L) return
        NotificationManagerCompat.from(context).cancel(scheduleId.toInt())
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        scheduleId: Long,
        patientId: Long,
        patientName: String,
        medicationName: String,
        timeString: String,
        frequencyName: String,
        startDate: String,
        endDate: String,
        delayMinutes: Int = 10
    ) {
        if (scheduleId == 0L || delayMinutes <= 0) return
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60_000L
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(
            context = context,
            requestCode = snoozeRequestCode(scheduleId),
            scheduleId = scheduleId,
            patientId = patientId,
            patientName = patientName,
            medicationName = medicationName,
            timeString = timeString,
            frequencyName = frequencyName,
            startDate = startDate,
            endDate = endDate,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun reminderPendingIntent(
        context: Context,
        scheduleId: Long,
        requestCode: Int = scheduleId.toInt(),
        patientId: Long,
        patientName: String,
        medicationName: String,
        timeString: String,
        frequencyName: String,
        startDate: String,
        endDate: String,
        flags: Int
    ): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
                putExtra(EXTRA_ALARM_PATIENT_ID, patientId)
                putExtra(EXTRA_PATIENT_NAME, patientName)
                putExtra(EXTRA_MEDICATION_NAME, medicationName)
                putExtra(EXTRA_SCHEDULED_TIME, timeString)
                putExtra(EXTRA_FREQUENCY, frequencyName)
                putExtra(EXTRA_START_DATE, startDate)
                putExtra(EXTRA_END_DATE, endDate)
            },
            flags
        )
    }

    private fun parseTimeToNextTrigger(
        timeString: String,
        frequencyName: String,
        startDate: String,
        endDate: String
    ): Long? {
        return runCatching {
            val parsed = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(timeString) ?: return null
            val now = Calendar.getInstance()
            val trigger = Calendar.getInstance().apply {
                time = parsed
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val start = parseDateOnly(startDate)
            if (start != null && trigger.before(start)) {
                trigger.set(Calendar.YEAR, start.get(Calendar.YEAR))
                trigger.set(Calendar.MONTH, start.get(Calendar.MONTH))
                trigger.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH))
            }

            val frequency = runCatching { DoseFrequency.valueOf(frequencyName) }
                .getOrDefault(DoseFrequency.ONCE_DAILY)
            val daysToAdd = if (frequency == DoseFrequency.WEEKLY) 7 else 1
            while (trigger.timeInMillis <= System.currentTimeMillis()) {
                trigger.add(Calendar.DAY_OF_YEAR, daysToAdd)
            }

            val end = parseDateOnly(endDate)
            if (end != null && trigger.after(end.endOfDay())) {
                return null
            }

            trigger.timeInMillis
        }.getOrNull()
    }

    private fun parseDateOnly(value: String): Calendar? {
        if (value.isBlank()) return null
        val parsed = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
        }.getOrNull() ?: return null
        return Calendar.getInstance().apply {
            time = parsed
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun Calendar.endOfDay(): Calendar {
        return (clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    private fun snoozeRequestCode(scheduleId: Long): Int = scheduleId.toInt() xor 0x5A5A0000
}

private fun ringingRequestCode(scheduleId: Long): Int = scheduleId.toInt() xor 0x0A1A0000

private const val CHANNEL_ID = "dose_reminders_alarm_v2"
private val VIBRATION_PATTERN = longArrayOf(0, 400, 200, 400, 200, 400)
const val EXTRA_SCHEDULE_ID = "schedule_id"
const val EXTRA_MEDICATION_NAME = "medication_name"
const val EXTRA_SCHEDULED_TIME = "scheduled_time"
const val EXTRA_FREQUENCY = "frequency"
const val EXTRA_START_DATE = "start_date"
const val EXTRA_END_DATE = "end_date"
const val EXTRA_PATIENT_NAME = "patient_name"
const val EXTRA_OPEN_DOSE_LOG = "open_dose_log"
const val EXTRA_ALARM_PATIENT_ID = "alarm_patient_id"
