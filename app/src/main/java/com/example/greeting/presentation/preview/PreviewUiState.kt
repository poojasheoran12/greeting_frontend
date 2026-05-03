package com.example.greeting.presentation.preview

import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.UserProfile

data class PreviewUiState(
    val template: Template? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSharing: Boolean = false,
    val error: String? = null
)

sealed class PreviewEvent {
    object ShareComplete : PreviewEvent()
    data class Error(val message: String) : PreviewEvent()
}
