package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.UserProfile

class UserProfileRepository(context: Context) {
    private val userProfileDao = AppDatabaseProvider.getDatabase(context).userProfileDao()
    private val preferences = context.getSharedPreferences("mediremind_profile", Context.MODE_PRIVATE)

    suspend fun getAllProfiles(): List<UserProfile> {
        return userProfileDao.getAllUserProfiles()
    }

    suspend fun getFirstUserProfile(): UserProfile? {
        return userProfileDao.getAllUserProfiles().firstOrNull()
    }

    suspend fun getActiveProfile(): UserProfile? {
        val activePatientId = getActivePatientId()
        val profiles = userProfileDao.getAllUserProfiles()
        return profiles.firstOrNull { it.id == activePatientId } ?: profiles.firstOrNull()
    }

    suspend fun getActivePatientId(): Long {
        val savedPatientId = preferences.getLong(KEY_ACTIVE_PATIENT_ID, 0L)
        if (savedPatientId != 0L) {
            return savedPatientId
        }

        val firstProfile = userProfileDao.getAllUserProfiles().firstOrNull()
        return firstProfile?.id ?: LEGACY_PATIENT_ID
    }

    fun setActivePatientId(patientId: Long) {
        preferences.edit().putLong(KEY_ACTIVE_PATIENT_ID, patientId).apply()
    }

    suspend fun saveUserProfile(userProfile: UserProfile): Long {
        val savedProfileId = if (userProfile.id == 0L) {
            userProfileDao.insertUserProfile(userProfile)
        } else {
            userProfileDao.updateUserProfile(userProfile)
            userProfile.id
        }
        setActivePatientId(savedProfileId)
        return savedProfileId
    }

    companion object {
        private const val KEY_ACTIVE_PATIENT_ID = "active_patient_id"
        const val LEGACY_PATIENT_ID = 1L
    }
}
