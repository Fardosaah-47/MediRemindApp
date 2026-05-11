package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.DoseSchedule

class DoseScheduleRepository(context: Context) {
    private val doseScheduleDao = AppDatabaseProvider.getDatabase(context).doseScheduleDao()

    suspend fun getAllDoseSchedules(): List<DoseSchedule> {
        return doseScheduleDao.getAllDoseSchedules()
    }

    suspend fun getSchedulesForPatient(patientId: Long): List<DoseSchedule> {
        return doseScheduleDao.getSchedulesForPatient(patientId)
    }

    suspend fun insertDoseSchedule(doseSchedule: DoseSchedule): Long {
        return doseScheduleDao.insertDoseSchedule(doseSchedule)
    }

    suspend fun insertDoseSchedules(doseSchedules: List<DoseSchedule>): List<Long> {
        return doseScheduleDao.insertDoseSchedules(doseSchedules)
    }

    suspend fun updateDoseSchedule(doseSchedule: DoseSchedule) {
        doseScheduleDao.updateDoseSchedule(doseSchedule)
    }

    suspend fun deleteDoseSchedule(doseSchedule: DoseSchedule) {
        doseScheduleDao.deleteDoseSchedule(doseSchedule)
    }

    suspend fun getSchedulesForMedication(medicationId: Long): List<DoseSchedule> {
        return doseScheduleDao.getSchedulesForMedication(medicationId)
    }

    suspend fun getSchedulesForMedicationForPatient(
        medicationId: Long,
        patientId: Long
    ): List<DoseSchedule> {
        return doseScheduleDao.getSchedulesForMedicationForPatient(medicationId, patientId)
    }
}
