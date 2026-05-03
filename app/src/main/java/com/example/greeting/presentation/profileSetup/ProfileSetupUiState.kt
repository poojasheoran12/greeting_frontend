package com.example.greeting.presentation.profileSetup

import android.net.Uri

data class ProfileSetupUiState(
    val name: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
