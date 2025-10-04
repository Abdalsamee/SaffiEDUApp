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
    private val DRAFT_SAVED_KEY = stringPreferencesKey("draft_saved_status") // ← تعريف المفتاح المفقود

    val draftFlow: Flow<Pair<AddLessonState?, Boolean>>
        get() = context.dataStore.data
            .map { prefs ->
                val json = prefs[DRAFT_KEY]
                val isSaved = prefs[DRAFT_SAVED_KEY] == "saved"
                val state = json?.let { gson.fromJson(it, AddLessonState::class.java) }
                state to isSaved
            }

    // حفظ المسودة فقط
    suspend fun saveDraft(state: AddLessonState, isButtonClick: Boolean = false) {
        val stateToSave = state.copy(
            selectedPdfUriString = state.selectedPdfUri?.toString(),  // تحويل Uri إلى String
            selectedVideoUriString = state.selectedVideoUri?.toString()
        )
        val json = gson.toJson(stateToSave)
        context.dataStore.edit { prefs ->
            prefs[DRAFT_KEY] = json
            if (isButtonClick) prefs[DRAFT_SAVED_KEY] = "saved"
        }
    }
}
