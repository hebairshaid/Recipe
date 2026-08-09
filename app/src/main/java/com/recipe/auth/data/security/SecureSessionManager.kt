package com.recipe.auth.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.recipe.auth.domain.session.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureSessionManager(context: Context) : SessionRepository {

    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "recipe_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))

    override fun observeToken(): Flow<String?> = _token.asStateFlow()

    override fun getToken(): String? = _token.value

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _token.value = token
    }

    override fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
        _token.value = null
    }

    override fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    private companion object {
        const val KEY_TOKEN = "session_token"
    }
}
