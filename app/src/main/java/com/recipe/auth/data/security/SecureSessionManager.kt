package com.recipe.auth.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.recipe.auth.domain.session.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecureSessionManager(context: Context) : SessionRepository {

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences = createPrefs()

    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))

    override fun observeToken(): Flow<String?> = _token.asStateFlow()

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null).also { stored ->
        if (_token.value != stored) {
            _token.value = stored
        }
    }

    override fun saveToken(token: String) {
        prefs.edit(commit = true) {
            putString(KEY_TOKEN, token)
        }
        _token.value = token
    }

    override fun clearToken() {
        prefs.edit(commit = true) {
            remove(KEY_TOKEN)
        }
        _token.value = null
    }

    override fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    private fun createPrefs(): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                appContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse {
            // Fallback so a broken crypto keystore never silently drops the session.
            appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private companion object {
        const val PREFS_NAME = "recipe_secure_session"
        const val FALLBACK_PREFS_NAME = "recipe_session"
        const val KEY_TOKEN = "session_token"
    }
}
