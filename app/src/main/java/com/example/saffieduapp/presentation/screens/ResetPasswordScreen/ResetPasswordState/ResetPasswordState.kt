package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.ResetPasswordState


// ✅ حالة الشاشة
data class ResetPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
