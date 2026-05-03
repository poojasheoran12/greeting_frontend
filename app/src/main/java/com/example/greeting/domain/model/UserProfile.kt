package com.example.greeting.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val photoUrl: String? = null,
    val isGuest: Boolean = false
)
