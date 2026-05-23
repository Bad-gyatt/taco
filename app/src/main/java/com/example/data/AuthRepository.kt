package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class AuthRepository(private val context: Context) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME = stringPreferencesKey("theme")
        val LANG = stringPreferencesKey("lang")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME] ?: "system" }
    val lang: Flow<String> = context.dataStore.data.map { it[LANG] ?: "uk" }

    suspend fun updateTheme(t: String) = context.dataStore.edit { it[THEME] = t }
    suspend fun updateLang(l: String) = context.dataStore.edit { it[LANG] = l }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: ""
    }

    suspend fun login(name: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_NAME] = name
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
