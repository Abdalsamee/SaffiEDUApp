package com.example.saffieduapp.presentation.screens.student.assignment_result

data class StudentAssignmentResultState(
    val isLoading: Boolean = true,

    // 🔹 بيانات الواجب
    val assignmentTitle: String = "",
    val files: List<String> = emptyList(),

    // 🔹 بيانات التقييم
    val studentName: String = "",
    val grade: String = "",
    val comment: String = "",

    // 🔹 لإدارة الحالة
    val errorMessage: String? = null
)
