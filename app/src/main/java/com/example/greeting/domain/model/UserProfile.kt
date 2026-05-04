package com.example.greeting.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String? = null,
    val photoUrl: String? = null,
    val localPhotoUri: String? = null,
    val isPhotoSyncPending: Boolean = false,
    val isGuest: Boolean = false
)
