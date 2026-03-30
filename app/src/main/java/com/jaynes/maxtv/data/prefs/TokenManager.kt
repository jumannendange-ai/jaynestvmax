package com.jaynes.maxtv.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.jaynes.maxtv.data.model.UserProfile
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jaynes_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN          = stringPreferencesKey("jwt_token")
        private val KEY_REFRESH_TOKEN  = stringPreferencesKey("refresh_token")
        private val KEY_USER_JSON      = stringPreferencesKey("user_json")
        private val KEY_ONBOARDING     = booleanPreferencesKey("onboarding_done")
        private val KEY_FIRST_INSTALL  = booleanPreferencesKey("first_install")
        private val gson = Gson()
    }

    // ── TOKEN ─────────────────────────────────────────────────

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun getToken(): String? = context.dataStore.data.first()[KEY_TOKEN]

    suspend fun saveTokens(token: String, refreshToken: String) {
        context.dataStore.edit {
            it[KEY_TOKEN]         = token
            it[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_REFRESH_TOKEN)
            it.remove(KEY_USER_JSON)
        }
    }

    fun getAuthHeader(token: String) = "Bearer $token"

    // ── USER PROFILE ──────────────────────────────────────────

    suspend fun saveUser(user: UserProfile) {
        context.dataStore.edit {
            it[KEY_USER_JSON] = gson.toJson(user)
        }
    }

    suspend fun getUser(): UserProfile? {
        val json = context.dataStore.data.first()[KEY_USER_JSON] ?: return null
        return try { gson.fromJson(json, UserProfile::class.java) } catch (e: Exception) { null }
    }

    val userFlow: Flow<UserProfile?> = context.dataStore.data.map {
        val json = it[KEY_USER_JSON] ?: return@map null
        try { gson.fromJson(json, UserProfile::class.java) } catch (e: Exception) { null }
    }

    // ── ONBOARDING ────────────────────────────────────────────

    suspend fun isOnboardingDone(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING] ?: false

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[KEY_ONBOARDING] = true }
    }

    suspend fun isFirstInstall(): Boolean =
        context.dataStore.data.first()[KEY_FIRST_INSTALL] ?: true

    suspend fun setFirstInstallDone() {
        context.dataStore.edit { it[KEY_FIRST_INSTALL] = false }
    }
}
