package com.example.greeting.presentation.home

import com.example.greeting.domain.model.Template

data class HomeState(
    val templates: List<Template> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
