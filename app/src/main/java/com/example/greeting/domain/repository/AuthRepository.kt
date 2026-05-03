package com.example.greeting.domain.repository

import com.example.greeting.domain.model.UserProfile
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    fun getCurrentUser(): UserProfile?
    suspend fun signInWithGoogle(credential: AuthCredential): Result<UserProfile>
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signUpWithEmail(email: String, password: String): Result<UserProfile>
    suspend fun signInAnonymously(): Result<UserProfile>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut()
}
