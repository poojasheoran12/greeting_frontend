package com.example.greeting.domain.repository

import android.net.Uri
import com.example.greeting.domain.model.UserProfile

interface UserRepository {
    fun getUserProfileFlow(uid: String): kotlinx.coroutines.flow.Flow<UserProfile?>
    suspend fun refreshUserProfile(uid: String): Result<Unit>
    suspend fun saveUserProfile(user: UserProfile): Result<Unit>
    suspend fun saveLocalProfilePhoto(uid: String, localUri: Uri): Result<Unit>
    suspend fun syncProfilePhoto(uid: String): Result<Unit>
}
