package com.example.saffieduapp.presentation.screens.SignUpScreen.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.AuthRepository
import com.example.saffieduapp.presentation.screens.SignUpScreen.Events.SignUpEvent
import com.example.saffieduapp.presentation.screens.SignUpScreen.model.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> _state.value = _state.value.copy(fullName = event.fullName)
            is SignUpEvent.IdNumberChanged -> _state.value = _state.value.copy(idNumber = event.idNumber)
            is SignUpEvent.EmailChanged -> _state.value = _state.value.copy(email = event.email)
            is SignUpEvent.PasswordChanged -> _state.value = _state.value.copy(password = event.password)
            is SignUpEvent.ConfirmPasswordChanged -> _state.value = _state.value.copy(confirmPassword = event.confirmPassword)
            is SignUpEvent.GradeChanged -> _state.value = _state.value.copy(selectedGrade = event.grade)
            is SignUpEvent.TermsAgreementChanged -> _state.value = _state.value.copy(agreedToTerms = event.agreed)
            is SignUpEvent.TogglePasswordVisibility -> _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
            is SignUpEvent.ToggleConfirmPasswordVisibility -> _state.value = _state.value.copy(isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible)
            is SignUpEvent.SignUpClicked -> viewModelScope.launch { signUpUser() }
        }
    }

    private suspend fun signUpUser() {
        val currentState = _state.value

        if (!validateInputs(currentState)) return

        _state.value = _state.value.copy(isLoading = true, signUpError = null)

        try {
            delay(1500)

            // 👈 تحقق من رقم الهوية حسب الدور
            if (authRepository.isIdNumberExists(currentState.idNumber, currentState.role)) {
                _state.value = _state.value.copy(isLoading = false)
                showError("رقم الهوية مستخدم مسبقًا")
                return
            }

            // 👈 إنشاء الحساب
            authRepository.createUserWithEmailAndPassword(
                email = currentState.email,
                password = currentState.password
            )

            // 👈 تخزين البيانات حسب الدور
            if (currentState.role == "student") {
                authRepository.registerStuData(
                    idNumber = currentState.idNumber,
                    fullName = currentState.fullName,
                    email = currentState.email,
                    grade = currentState.selectedGrade
                )
            } else {
                authRepository.registerTeacherData(
                    idNumber = currentState.idNumber,
                    fullName = currentState.fullName,
                    email = currentState.email,
                    subject = currentState.selectedSubject
                )
            }

            _state.value = _state.value.copy(isLoading = false)
            _eventFlow.emit(UiEvent.SignUpSuccessWithVerification)

        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false)
            showError("فشل التسجيل: ${e.message}")
        }
    }

    private fun validateInputs(state: SignUpState): Boolean {
        if (state.fullName.isEmpty() ||
            state.idNumber.isEmpty() ||
            state.email.isEmpty() ||
            state.password.isEmpty() ||
            state.confirmPassword.isEmpty() ||
            state.selectedGrade.isEmpty()
        ) {
            showError("يرجى ملء جميع الحقول")
            return false
        }

        if (!Regex("^[0-9]{9}$").matches(state.idNumber)) {
            showError("رقم الهوية يجب أن يكون 9 أرقام فقط")
            return false
        }

        if (!Regex("^[A-Za-z](.*)([@]{1})(.+)(\\.)(.+)").matches(state.email)) {
            showError("البريد الإلكتروني غير صالح")
            return false
        }

        if (state.password.length !in 8..16) {
            showError("كلمة المرور يجب أن تكون بين 8 و 16 حرفًا")
            return false
        }

        if (!state.password.any { it.isDigit() } || !state.password.any { it.isLetter() }) {
            showError("كلمة المرور يجب أن تحتوي على حروف وأرقام")
            return false
        }


        if (state.password != state.confirmPassword) {
            showError("كلمتا المرور غير متطابقتين")
            return false
        }

        if (!state.agreedToTerms) {
            showError("يجب الموافقة على الشروط والأحكام")
            return false
        }

        return true
    }

    private fun showError(message: String) {
        _state.value = _state.value.copy(signUpError = message)
        viewModelScope.launch {
            delay(3000)
            _state.value = _state.value.copy(signUpError = null)
        }
    }

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        object SignUpSuccessWithVerification : UiEvent()
    }
}
