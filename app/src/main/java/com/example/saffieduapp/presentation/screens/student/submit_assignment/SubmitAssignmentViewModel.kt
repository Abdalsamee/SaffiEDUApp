package com.example.saffieduapp.presentation.screens.student.submit_assignment

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.SubmissionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SubmitAssignmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val submissionRepository: SubmissionRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle,
    private val firestore : FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitAssignmentState())
    val state = _state.asStateFlow()

    private val assignmentId = savedStateHandle.get<String>("assignmentId") ?: ""

    init {
        val assignmentId = savedStateHandle.get<String>("assignmentId")
        if (assignmentId != null) {
            loadAssignmentDetails(assignmentId)
        }
    }

    fun submitAssignment(notes: String? = null) {
        viewModelScope.launch {
            val files = state.value.submittedFiles

            // جلب studentId بناءً على البريد الإلكتروني
            val doc = firestore.collection("students")
                .whereEqualTo("email", auth.currentUser?.email)
                .get()
                .await()

            val studentId = doc.documents.firstOrNull()?.id

            if (studentId == null) {
                // لم يتم العثور على الطالب
                _state.update { it.copy(isSubmitting = false, submissionSuccess = false) }
                return@launch
            }

            // تفعيل مؤشر التحميل
            _state.update { it.copy(isSubmitting = true) }

            val success = submissionRepository.submitAssignment(
                studentId = studentId,
                assignmentId = assignmentId,
                files = files.map { it.uri },
                context = context,
                notes = notes
            )
            // تسجيل وقت التسليم عند النجاح
            val currentTime = if (success) System.currentTimeMillis() else null

            // تحديث الحالة بعد التسليم
            _state.update { it.copy(isSubmitting = false, submissionSuccess = success, submissionTime = currentTime) }
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