package com.recipe.data.repository

import com.recipe.auth.data.local.ShoppingListDao
import com.recipe.auth.data.local.ShoppingListEntity
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import com.recipe.domain.model.ShoppingListItem
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
class ShoppingListRepository(
    private val shoppingListDao: ShoppingListDao,
    private val sessionRepository: SessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val items: StateFlow<List<ShoppingListItem>> = sessionRepository.observeToken()
        .map { token -> SessionToken.emailFrom(token) }
        .flatMapLatest { email ->
            if (email.isBlank()) {
                flowOf(emptyList())
            } else {
                shoppingListDao.observeByUser(email).map { entities ->
                    entities.map { it.toDomain() }
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    suspend fun add(name: String) {
        val email = currentUserEmail() ?: return
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        shoppingListDao.insert(
            ShoppingListEntity(
                userEmail = email,
                name = cleanName,
            ),
        )
    }

    suspend fun update(id: Long, name: String) {
        val email = currentUserEmail() ?: return
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        shoppingListDao.updateName(id, email, cleanName)
    }

    suspend fun toggleDone(id: Long, isDone: Boolean) {
        val email = currentUserEmail() ?: return
        shoppingListDao.updateDone(id, email, isDone)
    }

    suspend fun delete(id: Long) {
        val email = currentUserEmail() ?: return
        shoppingListDao.delete(id, email)
    }

    private fun currentUserEmail(): String? {
        val email = SessionToken.emailFrom(sessionRepository.getToken())
        return email.takeIf { it.isNotBlank() }
    }

    private fun ShoppingListEntity.toDomain(): ShoppingListItem = ShoppingListItem(
        id = id,
        name = name,
        isDone = isDone,
    )
}
