package com.recipe.auth.domain.model

data class AuthResponse(
    val user: User,
    val token: String,
)
