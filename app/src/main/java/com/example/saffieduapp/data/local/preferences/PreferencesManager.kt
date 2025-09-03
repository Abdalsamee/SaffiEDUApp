package com.example.saffieduapp.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.saffieduapp.data.local.preferences.PreferencesManager.Companion.SUBJECT_ACTIVATED_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit

// PreferencesManager.kt
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // نقل تعريف الـ DataStore إلى companion object لضمان أنه Singleton
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_credentials")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_PASSWORD = stringPreferencesKey("user_password")

        val SUBJECT_ACTIVATED_KEY = booleanPreferencesKey("subject_activated")

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



    val isSubjectActivated: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[SUBJECT_ACTIVATED_KEY] ?: false
        }

    suspend fun setSubjectActivated(activated: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SUBJECT_ACTIVATED_KEY] = activated
        }
    }
}