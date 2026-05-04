package com.example.greeting.domain.repository

import android.net.Uri
import com.example.greeting.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile?>
    suspend fun saveUserProfile(user: UserProfile): Result<Unit>
    suspend fun saveLocalProfilePhoto(uid: String, localUri: Uri): Result<Unit>
    suspend fun syncProfilePhoto(uid: String): Result<Unit>
}
