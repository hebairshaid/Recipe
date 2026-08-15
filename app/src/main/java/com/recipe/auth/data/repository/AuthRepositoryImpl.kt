package com.recipe.auth.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.recipe.auth.data.local.UserDao
import com.recipe.auth.data.local.UserEntity
import com.recipe.auth.domain.model.AuthResponse
import com.recipe.auth.domain.model.EmailValidator
import com.recipe.auth.domain.model.PasswordPolicy
import com.recipe.auth.domain.model.User
import com.recipe.auth.domain.repository.AuthRepository
import com.recipe.auth.domain.security.PasswordHasher
import com.recipe.auth.domain.session.SessionToken
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
) : AuthRepository {

    override suspend fun signUp(name: String, email: String, password: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanName.isBlank()) {
            throw IllegalArgumentException("Name is required")
        }
        EmailValidator.validate(cleanEmail)
        PasswordPolicy.validate(cleanPassword)

        if (isEmailExists(cleanEmail)) {
            throw IllegalArgumentException("Email already exists")
        }

        try {
            userDao.insertUser(
                UserEntity(
                    name = cleanName,
                    email = cleanEmail,
                    passwordHash = passwordHasher.hash(cleanPassword),
                ),
            )
        } catch (_: SQLiteConstraintException) {
            throw IllegalArgumentException("Email already exists")
        }
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        EmailValidator.validate(cleanEmail)
        if (cleanPassword.isBlank()) {
            throw IllegalArgumentException("Password is required")
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordHasher.verify(cleanPassword, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return AuthResponse(
            user = User(
                id = user.id,
                name = user.name,
                email = user.email,
            ),
            token = SessionToken.create(user.email),
        )
    }

    override suspend fun isEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email.trim().lowercase()) != null
    }

    override suspend fun getUserByEmail(email: String): User? {
        val entity = userDao.getUserByEmail(email.trim().lowercase()) ?: return null
        return User(
            id = entity.id,
            name = entity.name,
            email = entity.email,
        )
    }

    override suspend fun updatePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
    ) {
        val cleanEmail = email.trim().lowercase()
        val cleanCurrent = currentPassword.trim()
        val cleanNew = newPassword.trim()

        PasswordPolicy.validate(cleanNew)

        val user = userDao.getUserByEmail(cleanEmail)
            ?: throw IllegalArgumentException("User not found")

        if (!passwordHasher.verify(cleanCurrent, user.passwordHash)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        val updated = userDao.updatePasswordByEmail(
            email = cleanEmail,
            hashedPassword = passwordHasher.hash(cleanNew),
        )
        if (updated == 0) {
            throw IllegalArgumentException("Failed to update password")
        }
    }
}
