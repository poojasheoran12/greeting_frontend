package com.example.greeting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.greeting.data.local.dao.TemplateDao
import com.example.greeting.data.local.dao.UserDao
import com.example.greeting.data.local.entity.TemplateEntity
import com.example.greeting.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, TemplateEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val templateDao: TemplateDao
}
