package com.example.greeting.presentation.home

import androidx.paging.PagingData
import com.example.greeting.domain.model.Template
import kotlinx.coroutines.flow.Flow

data class HomeSection(
    val title: String,
    val templates: Flow<PagingData<Template>>
)

data class HomeUiState(
    val sections: List<HomeSection> = emptyList(),
    val selectedPremiumTemplate: Template? = null,
    val showPremiumDialog: Boolean = false
)

sealed class HomeUiEvent {
    data class NavigateToPreview(val templateId: String) : HomeUiEvent()
}
