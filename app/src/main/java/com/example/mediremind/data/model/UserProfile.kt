package com.example.mediremind.data.model

data class UserProfile(
    val id: Long = 0,
    val fullName: String,
    val age: Int? = null,
    val condition: String? = null,
    val caregiverName: String? = null
)
