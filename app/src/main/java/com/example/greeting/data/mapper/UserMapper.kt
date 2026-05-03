package com.example.greeting.data.mapper

import com.example.greeting.data.remote.dto.UserDto
import com.example.greeting.domain.model.UserProfile

fun UserDto.toDomain(): UserProfile {
    return UserProfile(
        uid = id ?: "",
        name = name ?: "",
        email = email,
        photoUrl = profileImageUrl,
        isGuest = isGuest ?: false
    )
}

fun UserProfile.toDto(): UserDto {
    return UserDto(
        id = uid,
        name = name,
        email = email,
        profileImageUrl = photoUrl,
        isGuest = isGuest
    )
}
