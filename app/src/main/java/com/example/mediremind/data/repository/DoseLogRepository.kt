package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.DoseLog

class DoseLogRepository(context: Context) {
    private val doseLogDao = AppDatabaseProvider.getDatabase(context).doseLogDao()

    suspend fun getAllDoseLogs(): List<DoseLog> {
        return doseLogDao.getAllDoseLogs()
    }

    suspend fun insertDoseLog(doseLog: DoseLog): Long {
        return doseLogDao.insertDoseLog(doseLog)
    }

    suspend fun updateDoseLog(doseLog: DoseLog) {
        doseLogDao.updateDoseLog(doseLog)
    }
}
