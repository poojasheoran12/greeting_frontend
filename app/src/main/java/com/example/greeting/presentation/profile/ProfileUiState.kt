package com.example.greeting.presentation.profile

import com.example.greeting.domain.model.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditNameDialog: Boolean = false
)

sealed class ProfileEvent {
    object LoggedOut : ProfileEvent()
}
