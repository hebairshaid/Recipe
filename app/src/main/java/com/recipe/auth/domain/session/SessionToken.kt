package com.recipe.auth.domain.session

import java.util.UUID

object SessionToken {
    private const val PREFIX = "recipe_token_"

    fun create(email: String): String {
        val cleanEmail = email.trim().lowercase()
        val unique = UUID.randomUUID().toString().replace("-", "").take(16)
        return "$PREFIX${cleanEmail}_$unique"
    }

    fun emailFrom(token: String?): String {
        if (token.isNullOrBlank() || !token.startsWith(PREFIX)) return ""
        val body = token.removePrefix(PREFIX)
        val separator = body.lastIndexOf('_')
        if (separator <= 0) return ""
        return body.substring(0, separator).trim().lowercase()
    }
}
