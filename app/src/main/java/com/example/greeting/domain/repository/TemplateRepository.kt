package com.example.greeting.domain.repository

import com.example.greeting.domain.model.Template

interface TemplateRepository {
    suspend fun getTemplates(): List<Template>
}
