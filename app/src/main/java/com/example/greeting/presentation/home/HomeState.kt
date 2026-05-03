package com.example.greeting.presentation.home

import com.example.greeting.domain.model.Template

data class HomeUiState(
    val groupedTemplates: Map<String, List<Template>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPremiumTemplate: Template? = null,
    val showPremiumDialog: Boolean = false
)

sealed class HomeUiEvent {
    data class NavigateToPreview(val templateId: String) : HomeUiEvent()
    data class ShowPremiumDialog(val template: Template) : HomeUiEvent()
}
