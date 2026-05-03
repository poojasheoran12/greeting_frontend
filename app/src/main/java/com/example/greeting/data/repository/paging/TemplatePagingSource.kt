package com.example.greeting.data.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.greeting.data.mapper.toDomain
import com.example.greeting.data.remote.dto.TemplateDto
import com.example.greeting.domain.model.Template
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class TemplatePagingSource(
    private val firestore: FirebaseFirestore,
    private val category: String
) : PagingSource<QuerySnapshot, Template>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Template> {
        return try {
            val currentPageQuery = params.key ?: firestore.collection("templates")
                .whereEqualTo("category", category)
                .orderBy("id") // Required for consistent paging
                .limit(params.loadSize.toLong())
                .get()
                .await()

            val lastDocument = currentPageQuery.documents.lastOrNull()
            val templates = currentPageQuery.toObjects(TemplateDto::class.java).map { it.toDomain() }

            val nextQuery = if (lastDocument != null && templates.size >= params.loadSize) {
                firestore.collection("templates")
                    .whereEqualTo("category", category)
                    .orderBy("id")
                    .startAfter(lastDocument)
                    .limit(params.loadSize.toLong())
                    .get()
                    .await()
            } else null

            LoadResult.Page(
                data = templates,
                prevKey = null,
                nextKey = nextQuery
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Template>): QuerySnapshot? {
        return null
    }
}
