package com.example.greeting.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        object NavigateToHome : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun onSaveProfile() {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userProfile = UserProfile(
                uid = currentUser.uid,
                name = _uiState.value.name,
                isGuest = currentUser.isGuest
            )
            
            userRepository.saveUserProfile(userProfile, _uiState.value.selectedImageUri)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _eventFlow.emit(UiEvent.NavigateToHome)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _eventFlow.emit(UiEvent.ShowError(e.message ?: "Failed to save profile"))
                }
        }
    }
}
