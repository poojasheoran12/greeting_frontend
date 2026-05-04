package com.example.greeting.presentation.profileSetup

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
    private val userRepository: UserRepository,
    private val workManager: androidx.work.WorkManager
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

    fun onImageSelected(uri: android.net.Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun onSaveProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowError("User session not found"))
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val selectedUri = _uiState.value.selectedImageUri
            
            // Step 1: Initial Profile (Optimistic)
            val userProfile = UserProfile(
                uid = currentUser.uid,
                name = _uiState.value.name,
                photoUrl = null,
                localPhotoUri = selectedUri?.toString(),
                isPhotoSyncPending = selectedUri != null,
                isGuest = currentUser.isGuest
            )
            
            // Step 2: Save Profile Structure
            userRepository.saveUserProfile(userProfile).onSuccess {
                // Step 3: Enqueue Background Photo Sync
                if (selectedUri != null) {
                    val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.greeting.core.sync.ProfilePhotoUploadWorker>()
                        .setInputData(androidx.work.workDataOf(com.example.greeting.core.sync.ProfilePhotoUploadWorker.KEY_UID to currentUser.uid))
                        .setConstraints(
                            androidx.work.Constraints.Builder()
                                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                    
                    workManager.enqueueUniqueWork(
                        "photo_sync_${currentUser.uid}",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        syncRequest
                    )
                }
                
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
                _eventFlow.emit(UiEvent.NavigateToHome)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _eventFlow.emit(UiEvent.ShowError(e.message ?: "Failed to save profile"))
            }
        }
    }
}
