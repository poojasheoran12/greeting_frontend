package com.example.greeting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.greeting.domain.model.UserProfile

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String?,
    val photoUrl: String?,
    val localPhotoUri: String?,
    val isPhotoSyncPending: Boolean,
    val isGuest: Boolean
)

fun UserEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        name = name,
        email = email,
        photoUrl = photoUrl,
        localPhotoUri = localPhotoUri,
        isPhotoSyncPending = isPhotoSyncPending,
        isGuest = isGuest
    )
}

fun UserProfile.toEntity(): UserEntity {
    return UserEntity(
        uid = uid,
        name = name,
        email = email,
        photoUrl = photoUrl,
        localPhotoUri = localPhotoUri,
        isPhotoSyncPending = isPhotoSyncPending,
        isGuest = isGuest
    )
}
