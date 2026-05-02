package com.example.greeting.data.repository

import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.remote.dto.UserDto
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val userProfile = firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(UserDto::class.java)
                ?.toDomain()
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
