package com.recipe.auth.domain.model

object PasswordPolicy {
    fun validate(password: String) {
        if (password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            throw IllegalArgumentException("Password must contain a capital letter")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            throw IllegalArgumentException("Password must contain a special character")
        }
    }
}
