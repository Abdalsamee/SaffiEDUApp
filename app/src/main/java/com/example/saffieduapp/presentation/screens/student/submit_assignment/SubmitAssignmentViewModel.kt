package com.example.saffieduapp.presentation.screens.student.submit_assignment

import android.annotation.SuppressLint
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
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitAssignmentState())
    val state = _state.asStateFlow()

    private val assignmentId = savedStateHandle.get<String>("assignmentId") ?: ""

    init {
        val id = savedStateHandle.get<String>("assignmentId")
        if (id != null) loadAssignmentDetails(id)
    }

    // تحميل تفاصيل الواجب والتحقق إن كان الطالب سلّمه من قبل
    private fun loadAssignmentDetails(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val studentEmail = auth.currentUser?.email ?: return@launch
            val studentDoc = firestore.collection("students")
                .whereEqualTo("email", studentEmail)
                .get()
                .await()

            val studentId = studentDoc.documents.firstOrNull()?.id ?: return@launch

            val submissionDoc = firestore.collection("assignment_submissions")
                .document("$id-$studentId")
                .get()
                .await()

            if (submissionDoc.exists()) {
                val fileUrls = submissionDoc.get("submittedFiles") as? List<String> ?: emptyList()
                val submittedFiles = fileUrls.mapIndexed { index, url ->
                    SubmittedFile(Uri.parse(url), "ملف ${index + 1}", 0L)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        alreadySubmitted = true,
                        submittedFiles = submittedFiles,
                        assignmentTitle = "تم تسليم الواجب",
                        subjectName = "عرض التسليم"
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        alreadySubmitted = false,
                        assignmentTitle = "النحو والصرف",
                        subjectName = "اللغة العربية"
                    )
                }
            }
        }
    }

    // تسليم جديد
    fun submitAssignment(notes: String? = null) {
        viewModelScope.launch {
            val files = state.value.submittedFiles

            val doc = firestore.collection("students")
                .whereEqualTo("email", auth.currentUser?.email)
                .get()
                .await()
            val studentId = doc.documents.firstOrNull()?.id ?: return@launch

            _state.update { it.copy(isSubmitting = true) }

            val success = submissionRepository.submitAssignment(
                studentId, assignmentId, files.map { it.uri }, context, notes
            )

            _state.update {
                it.copy(
                    isSubmitting = false,
                    submissionSuccess = success,
                    alreadySubmitted = success,
                    submissionTime = System.currentTimeMillis()
                )
            }
        }
    }

    // إعادة تسليم
    fun resubmitAssignment() {
        submitAssignment()
    }

    fun toggleEditMode() {
        _state.update { it.copy(isEditingSubmission = !it.isEditingSubmission) }
    }

    fun addFiles(uris: List<Uri>) {
        val newFiles = uris.mapNotNull { getSubmittedFileFromUri(it) }
        _state.update {
            it.copy(submittedFiles = it.submittedFiles + newFiles)
        }
    }

    fun removeFile(file: SubmittedFile) {
        _state.update {
            it.copy(submittedFiles = it.submittedFiles.filter { f -> f.uri != file.uri })
        }
    }

    fun clearAllFiles() {
        _state.update { it.copy(submittedFiles = emptyList()) }
    }

    fun resetSubmissionStatus() {
        _state.update { it.copy(submissionSuccess = false) }
    }

    @SuppressLint("Range")
    private fun getSubmittedFileFromUri(uri: Uri): SubmittedFile? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
                SubmittedFile(uri, name, size)
            } else null
        }
    }
}