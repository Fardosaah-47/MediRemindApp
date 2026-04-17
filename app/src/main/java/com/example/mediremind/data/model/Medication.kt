package com.example.mediremind.data.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val form: String,
    val dosage: String,
    val currentStockAmount: Int,
    val stockUnit: String,
    val refillAlertAt: Int
)
