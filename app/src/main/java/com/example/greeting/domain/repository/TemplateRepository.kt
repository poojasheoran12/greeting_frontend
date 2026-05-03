package com.example.greeting.domain.repository

import androidx.paging.PagingData
import com.example.greeting.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    suspend fun getTemplateById(id: String): Result<Template?>
    fun getTemplatesByCategoryPaged(category: String): Flow<PagingData<Template>>
}
