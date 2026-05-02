package com.example.greeting.data.repository

import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.remote.dto.TemplateDto
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.repository.TemplateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreTemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TemplateRepository {

    override suspend fun getTemplates(): Result<List<Template>> {
        return try {
            val templates = firestore.collection("templates")
                .get()
                .await()
                .toObjects(TemplateDto::class.java)
                .map { it.toDomain() }
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
