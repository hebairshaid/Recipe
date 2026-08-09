package com.recipe.auth.domain.session

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeToken(): Flow<String?>
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
    fun isLoggedIn(): Boolean
}
