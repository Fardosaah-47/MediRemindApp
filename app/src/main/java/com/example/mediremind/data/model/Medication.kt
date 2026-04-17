package com.example.mediremind.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val form: MedicationForm,
    val dosage: String,
    val currentStockAmount: Double,
    val stockUnit: String,
    val refillAlertAt: Double
)
