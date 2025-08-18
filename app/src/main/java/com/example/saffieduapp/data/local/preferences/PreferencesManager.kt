package com.example.saffieduapp.data.local.preferences

// PreferencesManager.kt
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_credentials")

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_PASSWORD = stringPreferencesKey("user_password")
    }

    suspend fun saveCredentials(id: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = id
            preferences[USER_PASSWORD] = password
        }
    }

    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USER_PASSWORD)
        }
    }

    fun getCredentials(): Flow<Pair<String?, String?>> {
        return context.dataStore.data
            .map { preferences ->
                Pair(
                    preferences[USER_ID],
                    preferences[USER_PASSWORD]
                )
            }
    }
}