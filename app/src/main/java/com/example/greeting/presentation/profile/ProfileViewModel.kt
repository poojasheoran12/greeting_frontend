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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
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
            _state.update { it.copy(isLoading = true) }
            val authUser = authRepository.getCurrentUser()
            if (authUser != null) {
                userRepository.getUserProfile(authUser.uid).onSuccess { profile ->
                    _state.update { it.copy(userProfile = profile ?: authUser, isLoading = false) }
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Not logged in") }
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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentProfile = _state.value.userProfile ?: return@launch
            
            // Step 1: Upload Image
            userRepository.uploadProfileImage(currentProfile.uid, uri).onSuccess { downloadUrl ->
                // Step 2: Update Profile with new URL
                val updatedProfile = currentProfile.copy(photoUrl = downloadUrl)
                userRepository.saveUserProfile(updatedProfile).onSuccess {
                    _state.update { it.copy(userProfile = updatedProfile, isLoading = false) }
                }.onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setShowEditNameDialog(show: Boolean) {
        _state.update { it.copy(showEditNameDialog = show) }
    }
}
