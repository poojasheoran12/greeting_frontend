package com.example.greeting.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class UserDto(
    @DocumentId
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val isGuest: Boolean? = false
)
