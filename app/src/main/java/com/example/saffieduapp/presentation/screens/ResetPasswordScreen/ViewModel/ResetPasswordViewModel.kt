package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.Model.ResetPasswordModel
import com.example.saffieduapp.presentation.screens.ResetPasswordScreen.ResetPasswordState.ResetPasswordState
//import com.example.saffieduapp.presentation.screens.resetpassword.model.ResetPasswordModel
//import com.example.saffieduapp.presentation.screens.resetpassword.state.ResetPasswordState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state

    fun onEmailChanged(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun sendResetLink() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, isSuccess = false, errorMessage = null)
            delay(1000) // محاكاة استدعاء API

            if (_state.value.email.contains("@")) {
                val user = ResetPasswordModel(_state.value.email)
                println("🔹 رابط إعادة تعيين أرسل إلى: ${user.email}")
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "البريد الإلكتروني غير صالح")
            }
        }
    }
}

