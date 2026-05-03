package com.example.greeting.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.greeting.data.local.entity.TemplateEntity

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates WHERE category = :category ORDER BY orderInRoom ASC")
    fun getTemplatesByCategory(category: String): PagingSource<Int, TemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Query("DELETE FROM templates WHERE category = :category")
    suspend fun deleteTemplatesByCategory(category: String)

    @Query("SELECT COUNT(*) FROM templates WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int
}
