package com.example.greeting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class AuthEvent {
        object NavigateToHome : AuthEvent()
        object NavigateToProfileSetup : AuthEvent()
        data class ShowError(val message: String) : AuthEvent()
    }

    init {
        _uiState.update { it.copy(user = authRepository.getCurrentUser()) }
    }

    fun onGoogleSignIn(credential: AuthCredential) {
        signIn { authRepository.signInWithGoogle(credential) }
    }

    fun onEmailSignIn(email: String, password: String) {
        signIn { authRepository.signInWithEmail(email, password) }
    }

    fun onAnonymousSignIn() {
        signIn { authRepository.signInAnonymously() }
    }

    private fun signIn(signInMethod: suspend () -> Result<UserProfile>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            signInMethod().onSuccess { user ->
                checkUserProfile(user.uid)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "Login failed"))
            }
        }
    }

    private suspend fun checkUserProfile(uid: String) {
        userRepository.getUserProfile(uid).onSuccess { profile ->
            _uiState.update { it.copy(isLoading = false) }
            if (profile != null) {
                _eventFlow.emit(AuthEvent.NavigateToHome)
            } else {
                _eventFlow.emit(AuthEvent.NavigateToProfileSetup)
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
            _eventFlow.emit(AuthEvent.NavigateToProfileSetup) // Default to setup if check fails
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(user = null) }
        }
    }
}
