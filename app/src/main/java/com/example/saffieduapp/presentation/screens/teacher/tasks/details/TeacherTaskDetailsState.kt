package com.example.saffieduapp.presentation.screens.teacher.tasks.details

data class TeacherTaskDetailsState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val students: List<StudentTaskItem> = emptyList()
)

data class StudentTaskItem(
    val id: String,
    val name: String,
    val score: String,
    val imageUrl: String
)
