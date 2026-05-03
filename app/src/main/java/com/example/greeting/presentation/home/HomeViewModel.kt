package com.example.greeting.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greeting.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        getTemplates()
    }

    fun getTemplates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            repository.getTemplates()
                .onSuccess { templates ->
                    // Group templates by category
                    val grouped = templates.groupBy { it.category }
                    _state.update { 
                        it.copy(
                            groupedTemplates = grouped, 
                            isLoading = false 
                        ) 
                    }
                }
                .onFailure { exception ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = exception.message ?: "Unknown error" 
                        ) 
                    }
                }
        }
    }
}
