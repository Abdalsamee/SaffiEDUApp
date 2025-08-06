package com.example.saffieduapp.presentation.screens.SignUpScreen.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.SignUpScreen.Events.SignUpEvent
import com.example.saffieduapp.presentation.screens.SignUpScreen.model.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> {
                _state.value = _state.value.copy(fullName = event.fullName)
            }
            is SignUpEvent.IdNumberChanged -> {
                _state.value = _state.value.copy(idNumber = event.idNumber)
            }
            is SignUpEvent.EmailChanged -> {
                _state.value = _state.value.copy(email = event.email)
            }
            is SignUpEvent.PasswordChanged -> {
                _state.value = _state.value.copy(password = event.password)
            }
            is SignUpEvent.ConfirmPasswordChanged -> {
                _state.value = _state.value.copy(confirmPassword = event.confirmPassword)
            }
            is SignUpEvent.GradeChanged -> {
                _state.value = _state.value.copy(selectedGrade = event.grade)
            }
            is SignUpEvent.TermsAgreementChanged -> {
                _state.value = _state.value.copy(agreedToTerms = event.agreed)
            }
            is SignUpEvent.TogglePasswordVisibility -> {
                _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
            }
            is SignUpEvent.ToggleConfirmPasswordVisibility -> {
                _state.value = _state.value.copy(isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible)
            }
            is SignUpEvent.SignUpClicked -> {
                signUpUser()
            }
        }
    }

    private fun signUpUser() {
        viewModelScope.launch {
            // TODO: Add validation for all fields
            _state.value = _state.value.copy(isLoading = true)
            kotlinx.coroutines.delay(1500) // Simulate network call

            // On success
            _state.value = _state.value.copy(isLoading = false)
            _eventFlow.emit(UiEvent.SignUpSuccess)
        }
    }

    sealed class UiEvent {
        data object SignUpSuccess : UiEvent()
    }
}