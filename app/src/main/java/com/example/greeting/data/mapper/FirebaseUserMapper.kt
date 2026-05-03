package com.example.greeting.data.mapper

import com.example.greeting.domain.model.UserProfile
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        name = displayName ?: "",
        email = email,
        photoUrl = photoUrl?.toString(),
        isGuest = isAnonymous
    )
}
