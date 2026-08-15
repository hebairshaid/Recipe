package com.recipe.domain.session

import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.session.SessionRepository
import com.recipe.auth.domain.session.SessionToken
import javax.inject.Inject

class ValidateSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): Boolean {
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
