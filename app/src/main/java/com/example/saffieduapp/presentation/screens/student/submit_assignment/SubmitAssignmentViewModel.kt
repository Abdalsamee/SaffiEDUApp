package com.example.saffieduapp.presentation.screens.student.submit_assignment

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitAssignmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // ١. حقن Context لجلب تفاصيل الملف
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitAssignmentState())
    val state = _state.asStateFlow()

    init {
        val assignmentId = savedStateHandle.get<String>("assignmentId")
        if (assignmentId != null) {
            loadAssignmentDetails(assignmentId)
        }
    }

    private fun loadAssignmentDetails(id: String) {
        // بيانات وهمية مؤقتة
        _state.value = SubmitAssignmentState(
            isLoading = false,
            assignmentTitle = "النحو والصرف",
            subjectName = "اللغة العربية"
        )
    }

    // --- ٢. إضافة منطق كامل لإدارة الملفات ---

    /**
     * دالة لإضافة قائمة من الملفات التي تم اختيارها.
     */
    fun addFiles(uris: List<Uri>) {
        val newFiles = uris.mapNotNull { getSubmittedFileFromUri(it) }
        _state.update { currentState ->
            currentState.copy(
                submittedFiles = currentState.submittedFiles + newFiles
            )
        }
    }

    /**
     * دالة لإزالة ملف محدد من القائمة.
     */
    fun removeFile(file: SubmittedFile) {
        _state.update { currentState ->
            currentState.copy(
                submittedFiles = currentState.submittedFiles.filter { it.uri != file.uri }
            )
        }
    }

    /**
     * دالة لمسح كل الملفات المختارة.
     */
    fun clearAllFiles() {
        _state.update { it.copy(submittedFiles = emptyList()) }
    }

    /**
     * دالة لتسليم الواجب.
     */
    fun submitAssignment() {
        viewModelScope.launch {
            // إظهار مؤشر التحميل في الزر
            _state.update { it.copy(isSubmitting = true) }

            // محاكاة عملية الرفع
            delay(2000)

            // إظهار رسالة النجاح وإخفاء مؤشر التحميل
            _state.update { it.copy(isSubmitting = false, submissionSuccess = true) }
        }
    }

    /**
     * دالة لإعادة تعيين حالة النجاح بعد إغلاق الديالوج.
     */
    fun resetSubmissionStatus() {
        _state.update { it.copy(submissionSuccess = false) }
    }

    /**
     * دالة مساعدة لجلب اسم وحجم الملف من الـ Uri.
     */
    private fun getSubmittedFileFromUri(uri: Uri): SubmittedFile? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)

                SubmittedFile(uri, name, size)
            } else {
                null
            }
        }
    }
}