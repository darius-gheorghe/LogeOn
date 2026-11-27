package com.sapphyx.logeon.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String? = null,
    val bio: String? = null,
    val dateOfBirthMillis: Long = 0L,
    val country: String = "",
    val onboardingCompleted: Boolean = false
)