package com.example.greeting.presentation.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.core.utils.GreetingBitmapRenderer
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
                _events.emit(PreviewEvent.Error("User not logged in"))
                return@launch
            }

            if (templateId == null) {
                _events.emit(PreviewEvent.Error("Template ID missing"))
                return@launch
            }

            // Fetch latest profile from Firestore
            userRepository.getUserProfile(authUser.uid).onSuccess { profile ->
                val user = profile ?: authUser // Fallback to auth info if firestore is empty
                
                templateRepository.getTemplateById(templateId).onSuccess { template ->
                    if (template != null) {
                        _uiState.update { it.copy(template = template, userProfile = user) }
                    } else {
                        _events.emit(PreviewEvent.Error("Template not found"))
                    }
                }.onFailure { error ->
                    _events.emit(PreviewEvent.Error(error.message ?: "Failed to fetch template"))
                }
            }.onFailure { error ->
                _events.emit(PreviewEvent.Error("Failed to fetch profile: ${error.message}"))
            }
        }
    }

    fun onShareClick() {
        val state = uiState.value
        val template = state.template ?: return
        val userProfile = state.userProfile ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }
            try {
                val bitmap = bitmapRenderer.render(template, userProfile)
                shareManager.shareImage(bitmap)
                _events.emit(PreviewEvent.ShareComplete)
            } catch (e: Exception) {
                _events.emit(PreviewEvent.Error(e.message ?: "Failed to share"))
            } finally {
                _uiState.update { it.copy(isSharing = false) }
            }
        }
    }
}
