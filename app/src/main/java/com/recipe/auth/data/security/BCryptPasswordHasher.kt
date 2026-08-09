package com.recipe.auth.data.security

import com.recipe.auth.domain.security.PasswordHasher
import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHasher : PasswordHasher {
    override fun hash(password: String): String {
        return BCrypt.hashpw(password.trim(), BCrypt.gensalt())
    }

    override fun verify(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            if (hashedPassword.isBlank()) false
            else BCrypt.checkpw(plainPassword.trim(), hashedPassword)
        } catch (_: Exception) {
            false
        }
    }
}
