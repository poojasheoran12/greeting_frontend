package com.example.greeting.di

import com.example.greeting.data.repository.AuthRepositoryImpl
import com.example.greeting.data.repository.FirestoreTemplateRepository
import com.example.greeting.data.repository.UserRepositoryImpl
import com.example.greeting.domain.repository.AuthRepository
import com.example.greeting.domain.repository.TemplateRepository
import com.example.greeting.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        templateRepositoryImpl: FirestoreTemplateRepository
    ): TemplateRepository
}
