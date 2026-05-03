package com.example.greeting.domain.repository

import com.example.greeting.domain.model.Template

interface TemplateRepository {
    suspend fun getTemplates(): Result<List<Template>>
    suspend fun getTemplateById(id: String): Result<Template?>
}
