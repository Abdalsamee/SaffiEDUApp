package com.example.saffieduapp.presentation.screens.login

sealed class LoginEvent {
    data class IdChanged(val id: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data class RememberMeChanged(val remember: Boolean) : LoginEvent()
    data object TogglePasswordVisibility : LoginEvent()
    data object LoginClicked : LoginEvent()
}
