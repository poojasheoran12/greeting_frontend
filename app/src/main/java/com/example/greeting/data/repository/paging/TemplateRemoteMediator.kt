package com.example.greeting.data.repository.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.greeting.data.local.AppDatabase
import com.example.greeting.data.local.entity.TemplateEntity
import com.example.greeting.data.local.entity.toEntity
import com.example.greeting.data.remote.dto.TemplateDto
import com.example.greeting.data.mapper.toDomain
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class TemplateRemoteMediator(
    private val firestore: FirebaseFirestore,
    private val database: AppDatabase,
    private val category: String
) : RemoteMediator<Int, TemplateEntity>() {

    private val TAG = "TemplateRemoteMediator"
    private var lastSnapshot: QuerySnapshot? = null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TemplateEntity>
    ): MediatorResult {
        return try {
            val loadSize = when (loadType) {
                LoadType.REFRESH -> 10 
                else -> state.config.pageSize
            }


            if (loadType != LoadType.REFRESH && lastSnapshot == null) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val query = if (loadType == LoadType.REFRESH) {
                firestore.collection("templates")
                    .whereEqualTo("category", category)
                    .orderBy(FieldPath.documentId())
                    .limit(loadSize.toLong())
            } else {
                val lastDoc = lastSnapshot?.documents?.lastOrNull()
                if (lastDoc == null) return MediatorResult.Success(endOfPaginationReached = true)
                
                firestore.collection("templates")
                    .whereEqualTo("category", category)
                    .orderBy(FieldPath.documentId())
                    .startAfter(lastDoc)
                    .limit(loadSize.toLong())
            }

            val snapshot = kotlinx.coroutines.withTimeout(5000L) {
                query.get(Source.SERVER).await()
            }
            val templates = snapshot.toObjects(TemplateDto::class.java).map { it.toDomain() }
            
            Log.d(TAG, "Fetched ${templates.size} templates for $category")
            snapshot.documents.forEach { doc ->
                Log.d(TAG, "  -> Raw Doc Data [${doc.id}]: ${doc.data}")
            }
            templates.forEach { 
                Log.d(TAG, "  -> Parsed Template ID: ${it.id} | isPremium: ${it.isPremium}")
            }
            
            lastSnapshot = snapshot

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.templateDao.deleteTemplatesByCategory(category)
                }
                
                val currentCount = if (loadType == LoadType.APPEND) {
                    database.templateDao.getCountByCategory(category)
                } else 0
                
                val entities = templates.mapIndexed { index, template ->
                    template.toEntity(order = currentCount + index)
                }
                database.templateDao.insertTemplates(entities)
                Log.d(TAG, "Saved ${entities.size} entities to Room for $category starting at $currentCount")
            }

            MediatorResult.Success(endOfPaginationReached = templates.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
