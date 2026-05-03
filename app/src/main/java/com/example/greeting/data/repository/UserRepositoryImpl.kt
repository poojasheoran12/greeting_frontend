package com.example.greeting.data.repository

import android.net.Uri
import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.mapper.toDto
import com.example.greeting.data.remote.dto.UserDto
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserRepository {

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val user = document.toObject(UserDto::class.java)?.toDomain()
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(user: UserProfile, imageUri: Uri?): Result<Unit> {
        return try {
            var finalPhotoUrl = user.photoUrl
            
            if (imageUri != null) {
                val storageRef = storage.reference.child("users/${user.uid}/profile.jpg")
                storageRef.putFile(imageUri).await()
                finalPhotoUrl = storageRef.downloadUrl.await().toString()
            }
            
            val updatedUser = user.copy(photoUrl = finalPhotoUrl)
            firestore.collection("users")
                .document(user.uid)
                .set(updatedUser.toDto())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
