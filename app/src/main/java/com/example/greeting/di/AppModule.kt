package com.example.greeting.di

import com.example.greeting.data.repository.FirestoreTemplateRepository
import com.example.greeting.data.repository.FirestoreUserRepository
import com.example.greeting.domain.repository.TemplateRepository
import com.example.greeting.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideTemplateRepository(firestore: FirebaseFirestore): TemplateRepository {
        return FirestoreTemplateRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): UserRepository {
        return FirestoreUserRepository(firestore)
    }
}
