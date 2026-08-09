package com.recipe.auth.domain.repository

import com.recipe.auth.domain.model.AuthResponse
import com.recipe.auth.domain.model.User

interface AuthRepository {
    suspend fun signUp(name: String, email: String, password: String)
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun isEmailExists(email: String): Boolean
    suspend fun getUserByEmail(email: String): User?
}
