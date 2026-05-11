package com.example.mediremind.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicationId"]), Index(value = ["patientId"])]
)
data class DoseSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long = 1,
    val medicationId: Long,
    val time: String,
    val frequency: DoseFrequency,
    val startDate: String = "",
    val endDate: String = ""
)
