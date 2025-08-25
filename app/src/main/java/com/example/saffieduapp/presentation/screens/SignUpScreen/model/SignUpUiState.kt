package com.example.saffieduapp.presentation.screens.SignUpScreen.model

data class SignUpState(
    val fullName: String = "",
    val idNumber: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedGrade: String = "",   // للطالب
    val selectedSubject: String = "", // للمعلم
    val role: String = "student",     // 👈 الافتراضي طالب
    val agreedToTerms: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val signUpError: String? = null
)

