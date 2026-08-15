package com.recipe.auth.domain.model

object EmailValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(email: String) {
        val clean = email.trim().lowercase()
        if (clean.isBlank()) {
            throw IllegalArgumentException("Email is required")
        }
        if (!emailRegex.matches(clean)) {
            throw IllegalArgumentException("Invalid email format")
        }
    }
}
