package com.example.greeting.presentation.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.presentation.rendering.GreetingBitmapRenderer
import com.example.greeting.core.utils.ImageShareManager
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.UserProfile
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.TemplateRepository
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
class PreviewViewModel @Inject constructor(
    private val bitmapRenderer: GreetingBitmapRenderer,
    private val shareManager: ImageShareManager,
    private val templateRepository: TemplateRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val templateId: String? = savedStateHandle["templateId"]

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PreviewEvent>()
    val events = _events.asSharedFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            val authUser = authRepository.getCurrentUser()
            if (authUser == null) {
                _uiState.update { it.copy(error = "User not logged in") }
                return@launch
            }

            if (templateId == null) {
                _uiState.update { it.copy(error = "Template ID missing") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch latest profile from Firestore
            userRepository.getUserProfile(authUser.uid).onSuccess { profile ->
                val user = profile ?: authUser
                
                templateRepository.getTemplateById(templateId).onSuccess { template ->
                    if (template != null) {
                        _uiState.update { it.copy(template = template, userProfile = user, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(error = "Template not found", isLoading = false) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Failed to fetch template", isLoading = false) }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to fetch profile: ${error.message}", isLoading = false) }
            }
        }
    }

    fun onShareClick() {
        val state = uiState.value
        val template = state.template ?: return
        val userProfile = state.userProfile ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }
            
            val renderResult = bitmapRenderer.render(template, userProfile)
            
            renderResult.onSuccess { bitmap ->
                shareManager.shareImage(bitmap)
                    .onSuccess {
                        _events.emit(PreviewEvent.ShareComplete)
                    }
                    .onFailure { e ->
                        _events.emit(PreviewEvent.Error(e.message ?: "Failed to share"))
                    }
            }.onFailure { e ->
                _events.emit(PreviewEvent.Error(e.message ?: "Failed to generate image."))
            }
            
            _uiState.update { it.copy(isSharing = false) }
        }
    }
}
