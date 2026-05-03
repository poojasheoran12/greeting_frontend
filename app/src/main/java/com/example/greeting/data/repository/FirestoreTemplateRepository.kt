package com.example.greeting.data.repository

import androidx.paging.*
import androidx.room.withTransaction
import com.example.greeting.data.local.AppDatabase
import com.example.greeting.data.local.dao.TemplateDao
import com.example.greeting.data.local.entity.toDomain
import com.example.greeting.data.local.entity.toEntity
import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.remote.dto.TemplateDto
import com.example.greeting.data.repository.paging.TemplateRemoteMediator
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.repository.TemplateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreTemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val templateDao: TemplateDao,
    private val database: AppDatabase
) : TemplateRepository {

    override suspend fun getTemplateById(id: String): Result<Template?> {
        return try {
            val template = firestore.collection("templates")
                .document(id)
                .get()
                .await()
                .toObject(TemplateDto::class.java)
                ?.toDomain()
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTemplatesByCategoryPaged(category: String): Flow<PagingData<Template>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            remoteMediator = TemplateRemoteMediator(firestore, database, category),
            pagingSourceFactory = { templateDao.getTemplatesByCategory(category) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }
}
