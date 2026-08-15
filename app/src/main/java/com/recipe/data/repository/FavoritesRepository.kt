package com.recipe.data.repository

import com.recipe.auth.data.local.FavoriteDao
import com.recipe.auth.data.local.FavoriteEntity
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import com.recipe.domain.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepository(
    private val favoriteDao: FavoriteDao,
    private val sessionRepository: SessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val favorites: StateFlow<List<Recipe>> = sessionRepository.observeToken()
        .map { token -> SessionToken.emailFrom(token) }
        .flatMapLatest { email ->
            if (email.isBlank()) {
                flowOf(emptyList())
            } else {
                favoriteDao.observeByUser(email).map { entities ->
                    entities.map { it.toRecipe() }
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    suspend fun toggle(recipe: Recipe) {
        val email = currentUserEmail() ?: return
        val existing = favoriteDao.getFavorite(email, recipe.id)
        if (existing != null) {
            favoriteDao.delete(email, recipe.id)
        } else {
            favoriteDao.insert(recipe.toEntity(email))
        }
    }

    suspend fun remove(recipeId: String) {
        val email = currentUserEmail() ?: return
        favoriteDao.delete(email, recipeId)
    }

    private fun currentUserEmail(): String? {
        val email = SessionToken.emailFrom(sessionRepository.getToken())
        return email.takeIf { it.isNotBlank() }
    }

    private fun FavoriteEntity.toRecipe(): Recipe = Recipe(
        id = recipeId,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredientCount = ingredientCount,
    )

    private fun Recipe.toEntity(userEmail: String): FavoriteEntity = FavoriteEntity(
        userEmail = userEmail,
        recipeId = id,
        name = name,
        imageUrl = imageUrl,
        category = category,
        area = area,
        ingredientCount = ingredientCount,
    )
}
