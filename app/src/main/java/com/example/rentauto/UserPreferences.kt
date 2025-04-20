package com.example.rentauto

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }


    val userId: Flow<Int?> = context.dataStore.data
        .map { it[USER_ID] }

    suspend fun saveUser(userId: Int?) {
        context.dataStore.edit { preferences ->
            if (userId != null) {
                preferences[USER_ID] = userId
                preferences[IS_LOGGED_IN] = true
            } else {
                preferences.remove(USER_ID)
                preferences[IS_LOGGED_IN] = false
            }
        }
    }

    suspend fun getUserId(): Int? {
        val prefs = context.dataStore.data.first()
        return prefs[USER_ID]
    }

    suspend fun logout() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
