package com.example.saffieduapp.data.FireBase

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.AddLessonState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "draft_lesson_store")

class DraftLessonManager(private val context: Context) {

    private val gson = Gson()
    private val DRAFT_KEY = stringPreferencesKey("draft_lesson")
    private val DRAFT_SAVED_KEY = stringPreferencesKey("draft_saved_status") // جديد

    suspend fun saveDraft(state: AddLessonState, isSavedButton: Boolean = true) {
        val json = gson.toJson(state)
        context.dataStore.edit { prefs ->
            prefs[DRAFT_KEY] = json
            prefs[DRAFT_SAVED_KEY] = if (isSavedButton) "saved" else "not_saved"
        }
    }

    val draftFlow: Flow<Pair<AddLessonState?, Boolean>> = context.dataStore.data
        .map { prefs ->
            val state = prefs[DRAFT_KEY]?.let { json -> gson.fromJson(json, AddLessonState::class.java) }
            val isSaved = prefs[DRAFT_SAVED_KEY] == "saved"
            state to isSaved
        }

    suspend fun clearDraft() {
        context.dataStore.edit { prefs ->
            prefs.remove(DRAFT_KEY)
            prefs.remove(DRAFT_SAVED_KEY)
        }
    }
}