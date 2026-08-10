package com.recipe.auth.domain.session

import java.util.UUID

object SessionToken {
    private const val PREFIX = "recipe_token|"

    fun create(email: String): String {
        val cleanEmail = email.trim().lowercase()
        val unique = UUID.randomUUID().toString().replace("-", "").take(16)
        return "$PREFIX$cleanEmail|$unique"
    }

    fun emailFrom(token: String?): String {
        if (token.isNullOrBlank()) return ""
        // Preferred format: recipe_token|email|unique
        if (token.startsWith(PREFIX)) {
            val parts = token.removePrefix(PREFIX).split('|')
            return parts.firstOrNull()?.trim()?.lowercase().orEmpty()
        }
        // Legacy format: recipe_token_email_unique
        val legacyPrefix = "recipe_token_"
        if (!token.startsWith(legacyPrefix)) return ""
        val body = token.removePrefix(legacyPrefix)
        val separator = body.lastIndexOf('_')
        if (separator <= 0) return ""
        return body.substring(0, separator).trim().lowercase()
    }
}
