package com.example.greeting.presentation.auth

import com.example.greeting.domain.model.UserProfile

data class AuthUiState(
    val user: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
