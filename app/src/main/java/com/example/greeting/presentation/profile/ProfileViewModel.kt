package com.example.greeting.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val workManager: androidx.work.WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser() ?: return@launch
            
            userRepository.getUserProfileFlow(authUser.uid)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .collect { profile ->
                    _state.update { it.copy(
                        userProfile = profile ?: authUser,
                        isLoading = false 
                    ) }
                }
        }
        
        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser() ?: return@launch
            userRepository.refreshUserProfile(authUser.uid).onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            val currentProfile = _state.value.userProfile ?: return@launch
            val updatedProfile = currentProfile.copy(name = newName)
            userRepository.saveUserProfile(updatedProfile).onSuccess {
                _state.update { it.copy(userProfile = updatedProfile, showEditNameDialog = false) }
            }
        }
    }

    fun updateProfilePhoto(uri: android.net.Uri) {
        val currentProfile = _state.value.userProfile ?: return
        
        viewModelScope.launch {
            // Optimistic Local Update
            userRepository.saveLocalProfilePhoto(currentProfile.uid, uri).onSuccess {
                // Instantly update UI from local source
                loadUserProfile() 
                
                // Enqueue background sync
                val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.greeting.core.sync.ProfilePhotoUploadWorker>()
                    .setInputData(androidx.work.workDataOf(com.example.greeting.core.sync.ProfilePhotoUploadWorker.KEY_UID to currentProfile.uid))
                    .setConstraints(
                        androidx.work.Constraints.Builder()
                            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
                
                workManager.enqueueUniqueWork(
                    "photo_sync_${currentProfile.uid}",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            }.onFailure { e ->
                _state.update { it.copy(error = "Failed to update local photo: ${e.message}") }
            }
        }
    }

    fun setShowEditNameDialog(show: Boolean) {
        _state.update { it.copy(showEditNameDialog = show) }
    }
}
