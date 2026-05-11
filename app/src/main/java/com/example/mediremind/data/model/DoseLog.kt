package com.example.mediremind.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = DoseSchedule::class,
            parentColumns = ["id"],
            childColumns = ["doseScheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["doseScheduleId"]),
        Index(value = ["medicationId"]),
        Index(value = ["patientId"])
    ]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long = 1,
    val doseScheduleId: Long,
    val medicationId: Long,
    val scheduledTime: String,
    val logDate: String,
    val status: DoseStatus,
    val takenAt: String? = null,
    val imageUri: String? = null
)
