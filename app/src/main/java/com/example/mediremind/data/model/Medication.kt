package com.example.mediremind.data.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val stockCount: Int,
    val refillThreshold: Int
)
