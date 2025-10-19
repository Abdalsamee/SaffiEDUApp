package com.example.saffieduapp.presentation.screens.student.profile

data class StudentProfileState(
    val isLoading: Boolean = true,

    // 🔹 بيانات الحساب
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val className: String = "",
    val average: String = "",
    val profileImageUrl: String? = null,

    // 🔹 لإدارة الحالة
    val errorMessage: String? = null,
    val message: String?
)
