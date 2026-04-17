package com.example.mediremind.data.model

data class DoseLog(
    val id: Long = 0,
    val doseScheduleId: Long,
    val medicationId: Long,
    val scheduledTime: String,
    val status: DoseStatus,
    val takenAt: String? = null,
    val imageUri: String? = null
)
