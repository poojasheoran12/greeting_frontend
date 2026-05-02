package com.example.greeting.domain.repository

import com.example.greeting.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(userId: String): UserProfile?
}
