package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.assignmnet

data class TeacherStudentAssignmentState(
    val isLoading: Boolean = false,
    val studentName: String = "",
    val studentClass: String = "",
    val deliveryStatus: String = "",
    val submittedFiles: List<SubmittedFile> = emptyList(),
    val grade: String = "",
    val comment: String = "",
    val errorMessage: String? = null
)

data class SubmittedFile(
    val fileName: String,
    val fileUrl: String,
    val isImage: Boolean // true = image, false = pdf
)
