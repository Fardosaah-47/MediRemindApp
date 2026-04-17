package com.example.mediremind.data.model

data class DoseSchedule(
    val id: Long = 0,
    val medicationId: Long,
    val time: String,
    val frequency: DoseFrequency
)
