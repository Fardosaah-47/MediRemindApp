package com.example.mediremind.data.model

data class Medication(
    val id: Long = 0,
    val name: String,
    val form: MedicationForm,
    val dosage: String,
    val currentStockAmount: Double,
    val stockUnit: String,
    val refillAlertAt: Double
)
