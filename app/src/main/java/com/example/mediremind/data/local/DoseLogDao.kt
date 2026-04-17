package com.example.mediremind.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediremind.data.model.DoseLog

@Dao
interface DoseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(doseLog: DoseLog): Long

    @Update
    suspend fun updateDoseLog(doseLog: DoseLog)

    @Delete
    suspend fun deleteDoseLog(doseLog: DoseLog)

    @Query("SELECT * FROM dose_logs ORDER BY id DESC")
    suspend fun getAllDoseLogs(): List<DoseLog>

    @Query("SELECT * FROM dose_logs WHERE doseScheduleId = :doseScheduleId ORDER BY id DESC")
    suspend fun getLogsForSchedule(doseScheduleId: Long): List<DoseLog>
}
