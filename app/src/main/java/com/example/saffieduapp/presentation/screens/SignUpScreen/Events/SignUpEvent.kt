package com.example.saffieduapp.presentation.screens.SignUpScreen.Events

sealed class SignUpEvent {
    data class IdChanged(val id: String): SignUpEvent()
    data class EmailChanged(val email: String): SignUpEvent()
    data class PasswordChanged(val password: String): SignUpEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String): SignUpEvent()
    object TogglePasswordVisibility: SignUpEvent()
    object ToggleConfirmPasswordVisibility: SignUpEvent()
    data class AgreedToTermsChanged(val agreed: Boolean): SignUpEvent()
    object SignupClicked: SignUpEvent()
}
