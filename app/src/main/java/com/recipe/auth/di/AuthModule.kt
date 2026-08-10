package com.recipe.auth.di

import android.content.Context
import com.recipe.auth.data.local.AuthDatabase
import com.recipe.auth.data.repository.AuthRepositoryImpl
import com.recipe.auth.data.security.BCryptPasswordHasher
import com.recipe.auth.data.security.SecureSessionManager
import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import com.recipe.data.local.RecipeCacheDatabase
import com.recipe.data.network.NetworkMonitor
import com.recipe.data.remote.NetworkModule
import com.recipe.data.repository.FavoritesRepository
import com.recipe.data.repository.RecipeRepository
import com.recipe.data.repository.ShoppingListRepository

class AuthDependencies(
    val authRepository: AuthRepository,
    val sessionRepository: SessionRepository,
    val favoritesRepository: FavoritesRepository,
    val shoppingListRepository: ShoppingListRepository,
    val recipeRepository: RecipeRepository,
    val networkMonitor: NetworkMonitor,
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
                val appContext = context.applicationContext
                val db = AuthDatabase.getInstance(appContext)
                val cacheDb = RecipeCacheDatabase.getInstance(appContext)
                val hasher = BCryptPasswordHasher()
                val sessionRepository = SecureSessionManager(appContext)
                val networkMonitor = NetworkMonitor(appContext)
                deps = AuthDependencies(
                    authRepository = AuthRepositoryImpl(db.userDao(), hasher),
                    sessionRepository = sessionRepository,
                    favoritesRepository = FavoritesRepository(
                        favoriteDao = db.favoriteDao(),
                        sessionRepository = sessionRepository,
                    ),
                    shoppingListRepository = ShoppingListRepository(
                        shoppingListDao = db.shoppingListDao(),
                        sessionRepository = sessionRepository,
                    ),
                    recipeRepository = RecipeRepository(
                        api = NetworkModule.api,
                        cacheDao = cacheDb.recipeCacheDao(),
                        networkMonitor = networkMonitor,
                    ),
                    networkMonitor = networkMonitor,
                )
            }
        }
    }

    fun get(): AuthDependencies {
        return deps ?: error("AuthModule.init(context) must be called first")
    }
}
