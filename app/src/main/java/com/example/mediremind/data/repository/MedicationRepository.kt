package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.Medication

class MedicationRepository(context: Context) {
    private val medicationDao = AppDatabaseProvider.getDatabase(context).medicationDao()

    suspend fun getAllMedications(): List<Medication> {
        return medicationDao.getAllMedications()
    }

    suspend fun getMedicationById(id: Long): Medication? {
        return medicationDao.getMedicationById(id)
    }

    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insertMedication(medication)
    }

    suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }
}
