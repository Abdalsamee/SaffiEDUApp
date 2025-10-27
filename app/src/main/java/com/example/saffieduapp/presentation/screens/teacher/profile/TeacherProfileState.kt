package com.example.saffieduapp.presentation.screens.teacher.profile

data class TeacherProfileState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val nationalId: String = "",
    val subject: String = "",
    val classesCount: Int = 0,
    val profileImageUrl: String? = null,
    val error: String? = null,
)
