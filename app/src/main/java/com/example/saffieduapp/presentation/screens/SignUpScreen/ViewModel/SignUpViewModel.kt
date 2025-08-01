package com.example.saffieduapp.presentation.screens.SignUpScreen.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.SignUpScreen.Events.SignUpEvent
import com.example.saffieduapp.presentation.screens.SignUpScreen.model.SignUpUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignUpViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onEvent(event: SignUpEvent) {
        when(event) {
            is SignUpEvent.IdChanged -> {
                _uiState.value = _uiState.value.copy(id = event.id)
            }
            is SignUpEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.email)
            }
            is SignUpEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _uiState.value = _uiState.value.copy(confirmPassword = event.confirmPassword)
            }
            SignUpEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
            }
            SignUpEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
            }
            is SignUpEvent.AgreedToTermsChanged -> {
                _uiState.value = _uiState.value.copy(agreedToTerms = event.agreed)
            }
            SignUpEvent.SignupClicked -> {
                signup()
            }
        }
    }

    private fun signup() {
        val state = _uiState.value

        // تحقق من صحة البيانات
        if (state.id.isBlank() || state.email.isBlank() ||
            state.password.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "يرجى تعبئة جميع الحقول")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "كلمة المرور وتأكيدها غير متطابقين")
            return
        }
        if (!state.agreedToTerms) {
            _uiState.value = state.copy(errorMessage = "يجب الموافقة على الأحكام والشروط")
            return
        }

        // بدء التحميل (محاكاة)
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            // هنا يمكنك تنفيذ عملية الاشتراك (شبكة، قاعدة بيانات، إلخ)

            // بعد النجاح
            _uiState.value = _uiState.value.copy(isLoading = false)
            // يمكن إضافة التنقل إلى شاشة أخرى
        }
    }
}
