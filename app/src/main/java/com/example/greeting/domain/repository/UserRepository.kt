package com.example.greeting.domain.repository

import android.net.Uri
import com.example.greeting.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile?>
    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String>
    suspend fun saveUserProfile(user: UserProfile): Result<Unit>
}
