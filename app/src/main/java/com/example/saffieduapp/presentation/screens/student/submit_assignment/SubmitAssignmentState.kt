package com.example.saffieduapp.presentation.screens.student.submit_assignment

import android.net.Uri

// ١. تعريف موديل للملف المرفق
data class SubmittedFile(
    val uri: Uri,
    val name: String,
    val size: Long
)

data class SubmitAssignmentState(
    val isLoading: Boolean = true,
    val assignmentTitle: String = "",
    val subjectName: String = "",
    val submittedFiles: List<SubmittedFile> = emptyList(),
    val isSubmitting: Boolean = false,
    val submissionSuccess: Boolean = false,
    val submissionTime: Long? = null,
    val alreadySubmitted: Boolean = false,
    val isEditingSubmission: Boolean = false
)