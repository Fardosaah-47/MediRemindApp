package com.example.mediremind.data.repository

import android.content.Context
import com.example.mediremind.data.local.AppDatabaseProvider
import com.example.mediremind.data.model.UserProfile

class UserProfileRepository(context: Context) {
    private val userProfileDao = AppDatabaseProvider.getDatabase(context).userProfileDao()

    suspend fun getFirstUserProfile(): UserProfile? {
        return userProfileDao.getAllUserProfiles().firstOrNull()
    }
}
