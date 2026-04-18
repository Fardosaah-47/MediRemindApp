package com.example.mediremind.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediremind.data.model.DoseSchedule

@Dao
interface DoseScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseSchedule(doseSchedule: DoseSchedule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseSchedules(doseSchedules: List<DoseSchedule>): List<Long>

    @Update
    suspend fun updateDoseSchedule(doseSchedule: DoseSchedule)

    @Delete
    suspend fun deleteDoseSchedule(doseSchedule: DoseSchedule)

    @Query("SELECT * FROM dose_schedules ORDER BY time ASC")
    suspend fun getAllDoseSchedules(): List<DoseSchedule>

    @Query("SELECT * FROM dose_schedules WHERE medicationId = :medicationId ORDER BY time ASC")
    suspend fun getSchedulesForMedication(medicationId: Long): List<DoseSchedule>
}
