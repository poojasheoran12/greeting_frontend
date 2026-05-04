package com.example.greeting.data.remote.datasource

import android.net.Uri
import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.remote.dto.UserDto
import com.example.greeting.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun fetchUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val userDto = withTimeout(5000L) {
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                    .toObject(UserDto::class.java)
            }
            Result.success(userDto?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("users/$uid/profile.jpg")
            withTimeout(20000L) {
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                Result.success(url)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImageUrl(uid: String, url: String): Result<Unit> {
        return try {
            withTimeout(5000L) {
                firestore.collection("users")
                    .document(uid)
                    .update("profileImageUrl", url)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(user: UserProfile): Result<Unit> {
        return try {
            withTimeout(5000L) {
                firestore.collection("users")
                    .document(user.uid)
                    .set(user) 
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
