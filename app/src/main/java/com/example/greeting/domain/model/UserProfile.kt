package com.example.greeting.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String? = null
)
