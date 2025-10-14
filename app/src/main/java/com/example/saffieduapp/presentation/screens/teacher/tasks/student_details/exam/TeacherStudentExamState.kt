package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

data class TeacherStudentExamState(
    val isLoading: Boolean = false,
    val studentName: String = "",
    val studentImage: String? = null,

    val earnedScore: String = "",
    val totalScore: String = "",
    val answerStatus: String = "",
    val totalTime: String = "",
    val examStatus: String = "",

    val cheatingLogs: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,

    val errorMessage: String? = null
)
