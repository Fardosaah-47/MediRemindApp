package com.example.mediremind.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    indices = [Index(value = ["patientId"])]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long = 1,
    val name: String,
    val form: MedicationForm,
    val dosage: String,
    val currentStockAmount: Double,
    val stockUnit: String,
    val refillAlertAt: Double,
    val referenceImageUri: String? = null,
    val isQrImported: Boolean = false
)
