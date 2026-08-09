package com.recipe.auth.di

import android.content.Context
import com.recipe.auth.data.local.AuthDatabase
import com.recipe.auth.data.repository.AuthRepositoryImpl
import com.recipe.auth.data.security.BCryptPasswordHasher
import com.recipe.auth.data.security.SecureSessionManager
import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import com.recipe.data.repository.FavoritesRepository

class AuthDependencies(
    val authRepository: AuthRepository,
    val sessionRepository: SessionRepository,
    val favoritesRepository: FavoritesRepository,
) {
    suspend fun hasValidSession(): Boolean {
        val token = sessionRepository.getToken() ?: return false
        val email = SessionToken.emailFrom(token)
        if (email.isBlank()) {
            sessionRepository.clearToken()
            return false
        }
        val user = authRepository.getUserByEmail(email)
        if (user == null) {
            sessionRepository.clearToken()
            return false
        }
        return true
    }
}

object AuthModule {
    @Volatile
    private var deps: AuthDependencies? = null

    fun init(context: Context) {
        if (deps != null) return
        synchronized(this) {
            if (deps == null) {
                val db = AuthDatabase.getInstance(context)
                val hasher = BCryptPasswordHasher()
                val sessionRepository = SecureSessionManager(context)
                deps = AuthDependencies(
                    authRepository = AuthRepositoryImpl(db.userDao(), hasher),
                    sessionRepository = sessionRepository,
                    favoritesRepository = FavoritesRepository(
                        favoriteDao = db.favoriteDao(),
                        sessionRepository = sessionRepository,
                    ),
                )
            }
        }
    }

    fun get(): AuthDependencies {
        return deps ?: error("AuthModule.init(context) must be called first")
    }
}
