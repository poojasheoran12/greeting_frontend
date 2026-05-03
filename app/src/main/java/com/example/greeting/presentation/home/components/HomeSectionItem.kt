package com.example.greeting.presentation.home.components

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.greeting.domain.model.Template
import com.example.greeting.presentation.home.HomeSection

@Composable
fun HomeSectionItem(
    section: HomeSection,
    onTemplateClick: (Template) -> Unit
) {
    val lazyPagingItems = section.templates.collectAsLazyPagingItems()
    CategorySection(
        title = section.title,
        templates = lazyPagingItems,
        onTemplateClick = onTemplateClick
    )
}
