package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.DoseSchedule

class DoseScheduleRepository(context: Context) {
    private val doseScheduleDao = AppDatabaseProvider.getDatabase(context).doseScheduleDao()

    suspend fun getAllDoseSchedules(): List<DoseSchedule> {
        return doseScheduleDao.getAllDoseSchedules()
    }

    suspend fun insertDoseSchedule(doseSchedule: DoseSchedule): Long {
        return doseScheduleDao.insertDoseSchedule(doseSchedule)
    }
}
