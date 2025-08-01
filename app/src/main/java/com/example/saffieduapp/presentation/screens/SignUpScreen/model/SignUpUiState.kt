package com.example.saffieduapp.presentation.screens.SignUpScreen.model

data class SignUpUiState(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val agreedToTerms: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
