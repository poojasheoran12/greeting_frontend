package com.example.greeting.di

import android.content.Context
import androidx.room.Room
import com.example.greeting.data.local.AppDatabase
import com.example.greeting.data.local.dao.UserDao
import com.example.greeting.data.repository.AuthRepositoryImpl
import com.example.greeting.data.repository.FirestoreTemplateRepository
import com.example.greeting.data.repository.UserRepositoryImpl
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.TemplateRepository
import com.example.greeting.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "greeting_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        userDao: UserDao
    ): UserRepository {
        return UserRepositoryImpl(firestore, storage, userDao)
    }

    @Provides
    @Singleton
    fun provideTemplateRepository(firestore: FirebaseFirestore): TemplateRepository {
        return FirestoreTemplateRepository(firestore)
    }
}
