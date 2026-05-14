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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mediremind.MainActivity
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.Medication
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.floor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RefillAlarmScheduler {
    fun evaluateAndSchedule(
        context: Context,
        medication: Medication,
        dosesPerDay: Int = 1
    ) {
        if (medication.refillAlertAt <= 0.0) {
            cancelFutureAlarm(context, medication.id)
            return
        }

        createRefillChannel(context)

        val stock = medication.currentStockAmount
        val threshold = medication.refillAlertAt

        when {
            stock <= 0.0 -> {
                postRefillNotification(
                    context = context,
                    medication = medication,
                    stockLeft = stock,
                    level = RefillLevel.OUT_OF_STOCK
                )
                cancelFutureAlarm(context, medication.id)
            }

            stock <= threshold -> {
                postRefillNotification(
                    context = context,
                    medication = medication,
                    stockLeft = stock,
                    level = RefillLevel.LOW_STOCK
                )
                cancelFutureAlarm(context, medication.id)
            }

            else -> {
                val dailyUse = dosesPerDay.coerceAtLeast(1)
                val daysUntilThreshold = ceil((stock - threshold) / dailyUse.toDouble())
                    .toInt()
                    .coerceAtLeast(1)
                scheduleFutureAlarm(
                    context = context,
                    medication = medication,
                    stockLeft = stock,
                    triggerAtMs = refillReminderTime(daysUntilThreshold)
                )
            }
        }
    }

    fun cancelFutureAlarm(context: Context, medicationId: Long) {
        val pendingIntent = buildFutureAlarmIntent(
            context = context,
            medicationId = medicationId,
            patientId = 0L,
            medicationName = "",
            stockUnit = "",
            stockLeft = 0.0,
            flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    internal fun postRefillNotification(
        context: Context,
        medication: Medication,
        stockLeft: Double,
        level: RefillLevel
    ) {
        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        if (!canNotify) return

        createRefillChannel(context)

        val tapIntent = PendingIntent.getActivity(
            context,
            (REFILL_NOTIFICATION_BASE + medication.id).toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_MEDICATIONS, true)
                putExtra(EXTRA_ALARM_PATIENT_ID, medication.patientId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stockDisplay = stockDisplay(stockLeft, medication.stockUnit)
        val title = when (level) {
            RefillLevel.OUT_OF_STOCK -> "Out of ${medication.name}"
            RefillLevel.LOW_STOCK -> "Low stock - ${medication.name}"
            RefillLevel.ADVANCE -> "Refill reminder - ${medication.name}"
        }
        val body = when (level) {
            RefillLevel.OUT_OF_STOCK ->
                "You have no ${medication.name} remaining. Refill as soon as possible."
            RefillLevel.LOW_STOCK ->
                "You only have $stockDisplay of ${medication.name}. Time to refill before you run out."
            RefillLevel.ADVANCE ->
                "Your ${medication.name} supply is getting low ($stockDisplay). Consider refilling soon."
        }

        NotificationManagerCompat.from(context).notify(
            (REFILL_NOTIFICATION_BASE + medication.id).toInt(),
            NotificationCompat.Builder(context, REFILL_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(
                    if (level == RefillLevel.OUT_OF_STOCK) {
                        NotificationCompat.PRIORITY_MAX
                    } else {
                        NotificationCompat.PRIORITY_HIGH
                    }
                )
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(tapIntent)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(if (level == RefillLevel.ADVANCE) longArrayOf(0, 180) else URGENT_VIBRATION)
                .build()
        )
    }

    fun createRefillChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(REFILL_CHANNEL_ID) != null) return

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            REFILL_CHANNEL_ID,
            "Refill reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when medication stock is running low"
            setSound(sound, audioAttributes)
            enableVibration(true)
            vibrationPattern = URGENT_VIBRATION
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            setShowBadge(true)
            enableLights(true)
            lightColor = 0xFFEF4444.toInt()
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleFutureAlarm(
        context: Context,
        medication: Medication,
        stockLeft: Double,
        triggerAtMs: Long
    ) {
        val pendingIntent = buildFutureAlarmIntent(
            context = context,
            medicationId = medication.id,
            patientId = medication.patientId,
            medicationName = medication.name,
            stockUnit = medication.stockUnit,
            stockLeft = stockLeft,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }

    private fun buildFutureAlarmIntent(
        context: Context,
        medicationId: Long,
        patientId: Long,
        medicationName: String,
        stockUnit: String,
        stockLeft: Double,
        flags: Int
    ): PendingIntent? {
        return PendingIntent.getBroadcast(
            context,
            (REFILL_ALARM_BASE + medicationId).toInt(),
            Intent(context, RefillAlarmReceiver::class.java).apply {
                putExtra(EXTRA_REFILL_MEDICATION_ID, medicationId)
                putExtra(EXTRA_REFILL_PATIENT_ID, patientId)
                putExtra(EXTRA_REFILL_MEDICATION_NAME, medicationName)
                putExtra(EXTRA_REFILL_STOCK_UNIT, stockUnit)
                putExtra(EXTRA_REFILL_STOCK_LEFT, stockLeft)
                putExtra(EXTRA_REFILL_ADVANCE_WARNING, true)
            },
            flags
        )
    }

    private fun refillReminderTime(daysUntilThreshold: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysUntilThreshold)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun stockDisplay(stockLeft: Double, unit: String): String {
        val amount = if (stockLeft == floor(stockLeft)) {
            stockLeft.toInt().toString()
        } else {
            String.format("%.1f", stockLeft)
        }
        return "$amount ${unit.ifBlank { "units" }}"
    }
}

class RefillAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_REFILL_MEDICATION_ID, 0L)
        val patientId = intent.getLongExtra(EXTRA_REFILL_PATIENT_ID, 0L)
        val medicationName = intent.getStringExtra(EXTRA_REFILL_MEDICATION_NAME) ?: "Medication"
        val stockUnit = intent.getStringExtra(EXTRA_REFILL_STOCK_UNIT) ?: "units"
        val stockLeftHint = intent.getDoubleExtra(EXTRA_REFILL_STOCK_LEFT, 0.0)
        val isAdvanceWarning = intent.getBooleanExtra(EXTRA_REFILL_ADVANCE_WARNING, false)

        RefillAlarmScheduler.createRefillChannel(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabaseProvider.getDatabase(context)
                val medication = database.medicationDao().getMedicationById(medicationId)

                if (medication == null) {
                    RefillAlarmScheduler.postRefillNotification(
                        context = context,
                        medication = Medication(
                            id = medicationId,
                            patientId = patientId,
                            name = medicationName,
                            form = com.example.mediremind.data.model.MedicationForm.OTHER,
                            dosage = "",
                            currentStockAmount = stockLeftHint,
                            stockUnit = stockUnit,
                            refillAlertAt = stockLeftHint
                        ),
                        stockLeft = stockLeftHint,
                        level = if (stockLeftHint <= 0.0) RefillLevel.OUT_OF_STOCK else RefillLevel.LOW_STOCK
                    )
                    return@launch
                }

                if (medication.refillAlertAt > 0.0) {
                    val level = when {
                        medication.currentStockAmount <= 0.0 -> RefillLevel.OUT_OF_STOCK
                        medication.currentStockAmount <= medication.refillAlertAt -> RefillLevel.LOW_STOCK
                        isAdvanceWarning -> RefillLevel.ADVANCE
                        else -> null
                    }

                    if (level == null) return@launch

                    RefillAlarmScheduler.postRefillNotification(
                        context = context,
                        medication = medication,
                        stockLeft = medication.currentStockAmount,
                        level = level
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

internal enum class RefillLevel {
    ADVANCE,
    LOW_STOCK,
    OUT_OF_STOCK
}

const val EXTRA_OPEN_MEDICATIONS = "open_medications"

private const val REFILL_CHANNEL_ID = "refill_reminders_v1"
private const val REFILL_ALARM_BASE = 10_000
private const val REFILL_NOTIFICATION_BASE = 20_000
private val URGENT_VIBRATION = longArrayOf(0, 300, 150, 300)

private const val EXTRA_REFILL_MEDICATION_ID = "refill_medication_id"
private const val EXTRA_REFILL_PATIENT_ID = "refill_patient_id"
private const val EXTRA_REFILL_MEDICATION_NAME = "refill_medication_name"
private const val EXTRA_REFILL_STOCK_UNIT = "refill_stock_unit"
private const val EXTRA_REFILL_STOCK_LEFT = "refill_stock_left"
private const val EXTRA_REFILL_ADVANCE_WARNING = "refill_advance_warning"
