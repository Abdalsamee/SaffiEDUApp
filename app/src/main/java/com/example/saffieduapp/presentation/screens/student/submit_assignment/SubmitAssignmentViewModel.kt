package com.example.saffieduapp.presentation.screens.student.submit_assignment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SubmitAssignmentViewModel @Inject constructor(
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

    // دوال لإدارة الملفات المرفقة (سيتم استدعاؤها من الواجهة)
    fun addFile(file: SubmittedFile) {
        // ...
    }

    fun removeFile(file: SubmittedFile) {
        // ...
    }

    fun clearAllFiles() {
        // ...
    }

    fun submitAssignment() {
        // ...
    }
}