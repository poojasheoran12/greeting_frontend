package com.example.greeting.presentation.home

import com.example.greeting.domain.model.Template

data class HomeUiState(
    val groupedTemplates: Map<String, List<Template>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
