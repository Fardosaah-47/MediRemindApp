package com.example.mediremind.domain

import com.example.mediremind.data.model.DoseLog
import com.example.mediremind.data.model.DoseStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val overallRatePercent: Int
)

data class DayAdherence(
    val dayLabel: String,
    val date: String,
    val scheduled: Int,
    val taken: Int,
    val rate: Float
)

data class WeeklySummary(
    val days: List<DayAdherence>,
    val weekTaken: Int,
    val weekScheduled: Int,
    val weekRatePercent: Int
)

object AdherenceEngine {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun computeStreak(logs: List<DoseLog>): StreakResult {
        val latestLogs = latestDoseLogs(logs)
        if (latestLogs.isEmpty()) return StreakResult(0, 0, 0)

        val totalTaken = latestLogs.count { it.status == DoseStatus.TAKEN }
        val overallRate = ((totalTaken.toFloat() / latestLogs.size.toFloat()) * 100).toInt()
        val byDate = latestLogs.groupBy { it.logDate }
        val knownDates = byDate.keys.sortedDescending()

        var current = 0
        var longest = 0
        var running = 0
        var currentStillOpen = true

        knownDates.forEach { date ->
            val dayLogs = byDate[date].orEmpty()
            val perfectDay = dayLogs.isNotEmpty() && dayLogs.all { it.status == DoseStatus.TAKEN }
            if (perfectDay) {
                running += 1
                longest = maxOf(longest, running)
                if (currentStillOpen) current = running
            } else {
                if (currentStillOpen) currentStillOpen = false
                running = 0
            }
        }

        return StreakResult(
            currentStreak = current,
            longestStreak = longest,
            overallRatePercent = overallRate
        )
    }

    fun computeWeeklySummary(logs: List<DoseLog>): WeeklySummary {
        val latestLogs = latestDoseLogs(logs)
        val today = Calendar.getInstance()
        val weekStart = (today.clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val logsByDate = latestLogs.groupBy { it.logDate }

        val days = (0..6).map { offset ->
            val day = (weekStart.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val date = dateFormat.format(day.time)
            val dayLogs = logsByDate[date].orEmpty()
            val scheduled = dayLogs.size
            val taken = dayLogs.count { it.status == DoseStatus.TAKEN }
            DayAdherence(
                dayLabel = dayFormat.format(day.time).take(3),
                date = date,
                scheduled = scheduled,
                taken = taken,
                rate = if (scheduled == 0) 0f else taken.toFloat() / scheduled.toFloat()
            )
        }

        val weekScheduled = days.sumOf { it.scheduled }
        val weekTaken = days.sumOf { it.taken }
        val weekRate = if (weekScheduled == 0) {
            0
        } else {
            ((weekTaken.toFloat() / weekScheduled.toFloat()) * 100).toInt()
        }

        return WeeklySummary(
            days = days,
            weekTaken = weekTaken,
            weekScheduled = weekScheduled,
            weekRatePercent = weekRate
        )
    }

    fun computeRecentDays(logs: List<DoseLog>, numDays: Int = 28): List<DayAdherence> {
        val latestLogs = latestDoseLogs(logs)
        val logsByDate = latestLogs.groupBy { it.logDate }
        val today = Calendar.getInstance()

        return (0 until numDays).map { offset ->
            val day = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            val date = dateFormat.format(day.time)
            val dayLogs = logsByDate[date].orEmpty()
            val scheduled = dayLogs.size
            val taken = dayLogs.count { it.status == DoseStatus.TAKEN }
            DayAdherence(
                dayLabel = dayFormat.format(day.time).take(3),
                date = date,
                scheduled = scheduled,
                taken = taken,
                rate = if (scheduled == 0) 0f else taken.toFloat() / scheduled.toFloat()
            )
        }
    }

    private fun latestDoseLogs(logs: List<DoseLog>): List<DoseLog> {
        return logs
            .groupBy { log -> "${log.logDate}|${log.doseScheduleId}" }
            .mapNotNull { (_, dayLogs) -> dayLogs.maxByOrNull { it.id } }
    }
}
