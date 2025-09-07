package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLessonViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AddLessonState())
    val state = _state.asStateFlow()
    val availableGrades = listOf(
        "الصف الأول", "الصف الثاني", "الصف الثالث", "الصف الرابع",
        "الصف الخامس", "الصف السادس", "الصف السابع", "الصف الثامن",
        "الصف التاسع", "الصف العاشر", "الصف الحادي عشر", "الصف الثاني عشر"
    )

    fun onEvent(event: AddLessonEvent) {
        // --- تم تنظيف جملة when من التكرار ---
        when (event) {
            is AddLessonEvent.TitleChanged -> {
                _state.update { it.copy(lessonTitle = event.title) }
            }
            is AddLessonEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is AddLessonEvent.ClassSelected -> {
                _state.update { it.copy(selectedClass = event.className) }
            }
            is AddLessonEvent.VideoSelected -> {
                _state.update { it.copy(
                    selectedVideoUri = event.uri,
                    selectedVideoName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.VIDEO else ContentType.NONE,
                    selectedPdfUri = null,
                    selectedPdfName = null
                ) }
            }
            is AddLessonEvent.PdfSelected -> {
                _state.update { it.copy(
                    selectedPdfUri = event.uri,
                    selectedPdfName = event.uri?.let { uri -> getFileName(uri) },
                    selectedContentType = if (event.uri != null) ContentType.PDF else ContentType.NONE,
                    selectedVideoUri = null,
                    selectedVideoName = null
                ) }
            }
            is AddLessonEvent.ClearVideoSelection -> {
                _state.update { it.copy(
                    selectedVideoUri = null,
                    selectedVideoName = null,
                    // أعد النوع إلى "لا شيء" إذا لم يكن هناك ملف PDF مختار
                    selectedContentType = if (it.selectedPdfUri == null) ContentType.NONE else ContentType.PDF
                ) }
            }
            is AddLessonEvent.ClearPdfSelection -> {
                _state.update { it.copy(
                    selectedPdfUri = null,
                    selectedPdfName = null,
                    // أعد النوع إلى "لا شيء" إذا لم يكن هناك فيديو مختار
                    selectedContentType = if (it.selectedVideoUri == null) ContentType.NONE else ContentType.VIDEO
                ) }
            }
            is AddLessonEvent.SaveClicked -> {
                saveLesson()
            }
            is AddLessonEvent.DateChanged -> {
                _state.update { it.copy(publicationDate = event.date) }
            }
            is AddLessonEvent.NotifyStudentsToggled -> {
                _state.update { it.copy(notifyStudents = event.isEnabled) }
            }
            is AddLessonEvent.SaveClicked -> {
                saveLesson()
            }
        }
    }

    private fun saveLesson() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            println("Saving lesson: ${state.value}")
            kotlinx.coroutines.delay(1500)
            _state.update { it.copy(isSaving = false) }
        }
    }

    // --- تم نقل الدالة إلى داخل الكلاس ---
    private fun getFileName(uri: Uri): String? {
        // الآن يمكنها الوصول إلى context بشكل صحيح
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}
