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
        data class ShowMessage(val message: String) : AuthEvent() // Correct semantic for success
    }

    init {
        _uiState.update { it.copy(user = authRepository.getCurrentUser()) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun toggleFormType() {
        _uiState.update { it.copy(isLoginForm = !it.isLoginForm) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    private fun validateCredentials(email: String, password: String? = null): String? {
        if (!isValidEmail(email)) return "Invalid email format"
        if (password != null && password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    fun onGoogleSignIn(credential: AuthCredential) {
        signIn { authRepository.signInWithGoogle(credential) }
    }

    fun onEmailSignIn(email: String, password: String) {
        val error = validateCredentials(email, password)
        if (error != null) {
            viewModelScope.launch { _eventFlow.emit(AuthEvent.ShowError(error)) }
            return
        }
        signIn { authRepository.signInWithEmail(email, password) }
    }

    fun onEmailSignUp(email: String, password: String) {
        val error = validateCredentials(email, password)
        if (error != null) {
            viewModelScope.launch { _eventFlow.emit(AuthEvent.ShowError(error)) }
            return
        }
        signIn { authRepository.signUpWithEmail(email, password) }
    }

    fun onAnonymousSignIn() {
        signIn { authRepository.signInAnonymously() }
    }

    fun onForgotPassword(email: String) {
        if (!isValidEmail(email)) {
            viewModelScope.launch { _eventFlow.emit(AuthEvent.ShowError("Please enter a valid email")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordResetEmail(email).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AuthEvent.ShowMessage("Reset link sent to $email"))
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "Failed to send reset link"))
            }
        }
    }

    private fun signIn(signInMethod: suspend () -> Result<UserProfile>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            signInMethod().onSuccess { user ->
                checkUserProfile(user.uid)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                _eventFlow.emit(AuthEvent.ShowError(e.message ?: "Authentication failed"))
            }
        }
    }

    private suspend fun checkUserProfile(uid: String) {
        // Force refresh from Firestore to check if user is returning or new
        userRepository.refreshUserProfile(uid)
        
        // Check Room for the result
        val profile = userRepository.getUserProfileFlow(uid).first()
        
        _uiState.update { it.copy(isLoading = false) }
        
        if (profile != null) {
            _eventFlow.emit(AuthEvent.NavigateToHome)
        } else {
            _eventFlow.emit(AuthEvent.NavigateToProfileSetup)
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(user = null) }
        }
    }
}
