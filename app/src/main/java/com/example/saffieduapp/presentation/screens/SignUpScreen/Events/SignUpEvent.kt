package com.example.saffieduapp.presentation.screens.SignUpScreen.Events

sealed class SignUpEvent {
    data class FullNameChanged(val fullName: String) : SignUpEvent()
    data class IdNumberChanged(val idNumber: String) : SignUpEvent()
    data class EmailChanged(val email: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent()
    data class GradeChanged(val grade: String) : SignUpEvent()
    data class TermsAgreementChanged(val agreed: Boolean) : SignUpEvent()
    data object TogglePasswordVisibility : SignUpEvent()
    data object ToggleConfirmPasswordVisibility : SignUpEvent()
    data object SignUpClicked : SignUpEvent()
}
