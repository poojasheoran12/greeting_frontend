package com.example.greeting.data.repository

import android.net.Uri
import com.example.greeting.data.local.dao.UserDao
import com.example.greeting.data.local.entity.toDomain
import com.example.greeting.data.local.entity.toEntity
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
    private val storage: FirebaseStorage,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {

            val localUser = userDao.getUserById(uid)
            if (localUser != null) {
                return Result.success(localUser.toDomain())
            }


            val userDto = firestore.collection("users")
                .document(uid)
                .get()
                .await()
                .toObject(UserDto::class.java)

            val profile = userDto?.toDomain()
            

            profile?.let {
                userDao.insertUser(it.toEntity())
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(user: UserProfile, imageUri: Uri?): Result<Unit> {
        return try {
            var updatedUser = user

            // 1. Upload Image if present
            if (imageUri != null) {
                val ref = storage.reference.child("profiles/${user.uid}.jpg")
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                updatedUser = user.copy(photoUrl = url)
            }

            // 2. Save to Firestore
            firestore.collection("users")
                .document(user.uid)
                .set(updatedUser.toDto())
                .await()

            // 3. Save to Room (SSOT)
            userDao.insertUser(updatedUser.toEntity())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
