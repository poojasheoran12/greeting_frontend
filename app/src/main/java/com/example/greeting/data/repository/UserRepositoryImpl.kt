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
import kotlinx.coroutines.withTimeout
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

            val userDto = withTimeout(5000L) {
                firestore.collection("users")
                    .document(uid)
                    .get(com.google.firebase.firestore.Source.DEFAULT)
                    .await()
                    .toObject(UserDto::class.java)
            }

            val profile = userDto?.toDomain()
            

            profile?.let {
                userDao.insertUser(it.toEntity())
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
        return try {
            val ref = storage.reference.child("users/$uid/profile.jpg")
            withTimeout(15000L) { ref.putFile(uri).await() }
            val url = withTimeout(5000L) { ref.downloadUrl.await().toString() }
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(user: UserProfile): Result<Unit> {
        return try {
            withTimeout(5000L) {
                firestore.collection("users")
                    .document(user.uid)
                    .set(user.toDto())
                    .await()
            }

            userDao.insertUser(user.toEntity())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
