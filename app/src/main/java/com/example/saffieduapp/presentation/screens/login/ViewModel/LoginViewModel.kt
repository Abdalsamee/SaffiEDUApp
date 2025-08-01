package com.example.saffieduapp.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.IdChanged -> {
                _uiState.value = _uiState.value.copy(id = event.id)
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }
            is LoginEvent.RememberMeChanged -> {
                _uiState.value = _uiState.value.copy(rememberMe = event.remember)
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            is LoginEvent.LoginClicked -> {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // ⏳ محاكاة عملية تسجيل الدخول
            kotlinx.coroutines.delay(1500)

            if (_uiState.value.id.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "يرجى إدخال جميع البيانات"
                )
            } else {
                // ✅ نجاح تسجيل الدخول
                _uiState.value = _uiState.value.copy(isLoading = false)
                // هنا يمكن الانتقال للصفحة التالية
            }
        }
    }
}
