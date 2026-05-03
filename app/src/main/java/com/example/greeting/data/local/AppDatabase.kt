package com.example.greeting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.greeting.data.local.dao.UserDao
import com.example.greeting.data.local.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}
