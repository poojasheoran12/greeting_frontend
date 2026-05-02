package com.example.greeting.presentation.explore

import com.example.greeting.domain.model.Template

data class ExploreState(
    val templates: List<Template> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
