package com.example.saffieduapp.presentation.screens.login

data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
