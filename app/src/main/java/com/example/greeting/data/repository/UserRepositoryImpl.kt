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

            val localUser = userDao.getUserById(uid)?.toDomain()

            val userDto = withTimeout(5000L) {
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                    .toObject(UserDto::class.java)
            }

            val remoteProfile = userDto?.toDomain()
            
            val finalProfile = if (localUser?.isPhotoSyncPending == true && remoteProfile != null) {
                remoteProfile.copy(
                    localPhotoUri = localUser.localPhotoUri,
                    isPhotoSyncPending = true
                )
            } else {
                remoteProfile ?: localUser
            }
            

            finalProfile?.let {
                userDao.insertUser(it.toEntity())
            }

            Result.success(finalProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> {
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

    override suspend fun saveLocalProfilePhoto(uid: String, localUri: Uri): Result<Unit> {
        return try {
            val userEntity = userDao.getUserById(uid) ?: return Result.failure(Exception("User not found"))
            val updatedUser = userEntity.toDomain().copy(
                localPhotoUri = localUri.toString(),
                isPhotoSyncPending = true
            )
            userDao.insertUser(updatedUser.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncProfilePhoto(uid: String): Result<Unit> {
        return try {
            val userEntity = userDao.getUserById(uid) ?: return Result.failure(Exception("User not found"))
            val user = userEntity.toDomain()
            val localUriStr = user.localPhotoUri ?: return Result.success(Unit)
            val localUri = Uri.parse(localUriStr)


            val downloadUrl = uploadProfileImage(uid, localUri).getOrThrow()


            withTimeout(5000L) {
                firestore.collection("users")
                    .document(uid)
                    .update("profileImageUrl", downloadUrl)
                    .await()
            }

            val finalUser = user.copy(
                photoUrl = downloadUrl,
                localPhotoUri = null,
                isPhotoSyncPending = false
            )
            userDao.insertUser(finalUser.toEntity())
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
