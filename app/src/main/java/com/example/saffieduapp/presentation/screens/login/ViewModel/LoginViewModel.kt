package com.example.saffieduapp.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ١. إضافة Hilt للسماح بحقن التبعيات لاحقًا
@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // ٢. تعريف الأحداث التي تحدث لمرة واحدة (مثل التنقل)
    sealed class UiEvent {
        data object LoginSuccess : UiEvent()
    }

    // ٣. إنشاء مجرى خاص بهذه الأحداث
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


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
            kotlinx.coroutines.delay(1500)

            if (_uiState.value.id.isEmpty() || _uiState.value.password.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "يرجى إدخال جميع البيانات"
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)

                // ٤. إرسال حدث النجاح إلى الواجهة لتنفيذ الانتقال
                _eventFlow.emit(UiEvent.LoginSuccess)
            }
        }
    }
}