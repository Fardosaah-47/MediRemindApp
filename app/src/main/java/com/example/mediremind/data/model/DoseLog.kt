package com.example.mediremind.data.model

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val scheduledTime: String,
    val status: String,
    val takenAt: String? = null
)
