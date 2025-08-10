package com.example.saffieduapp.presentation.screens.SignUpScreen.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.AuthRepository
import com.example.saffieduapp.presentation.screens.SignUpScreen.Events.SignUpEvent
import com.example.saffieduapp.presentation.screens.SignUpScreen.model.SignUpState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
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

        // التحقق من الحقول الفارغة
        if (currentState.fullName.isBlank() ||
            currentState.idNumber.isBlank() ||
            currentState.email.isBlank() ||
            currentState.password.isBlank() ||
            currentState.confirmPassword.isBlank() ||
            currentState.selectedGrade.isBlank()
        ) {
            showError("يرجى ملء جميع الحقول")
            return
        }

        // التحقق من رقم الهوية
        val idRegex = Regex("^[0-9]{9}$")
        if (!idRegex.matches(currentState.idNumber)) {
            showError("رقم الهوية يجب أن يكون 9 أرقام فقط")
            return
        }

        // التحقق من البريد الإلكتروني
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.+)(\\.)(.+)")
        if (!emailRegex.matches(currentState.email)) {
            showError("البريد الإلكتروني غير صالح")
            return
        }

        // كلمة المرور
        if (currentState.password.length < 6) {
            showError("كلمة المرور يجب أن تكون 6 أحرف على الأقل")
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            showError("كلمتا المرور غير متطابقتين")
            return
        }

        // الموافقة على الشروط
        if (!currentState.agreedToTerms) {
            showError("يجب الموافقة على الشروط والأحكام")
            return
        }

        _state.value = _state.value.copy(isLoading = true, signUpError = null)

        try {
            delay(3000) // تقليل الطلبات
            // تحقق من رقم الهوية
            val exists = authRepository.isIdNumberExists(currentState.idNumber)
            if (exists) {
                _state.value = _state.value.copy(isLoading = false)
                showError("رقم الهوية مستخدم مسبقًا")
                return
            }

            // إنشاء حساب في Auth
            authRepository.createUserWithEmailAndPassword(
                email = currentState.email,
                password = currentState.password
            )

            // إرسال بريد التحقق
            firebaseAuth.currentUser?.sendEmailVerification()?.await()

            // حفظ باقي البيانات في Firestore بدون كلمة السر
            authRepository.registerUserData(
                idNumber = currentState.idNumber,
                fullName = currentState.fullName,
                email = currentState.email,
                grade = currentState.selectedGrade
            )

            _state.value = _state.value.copy(isLoading = false)

            // إرسال حدث نجاح مع الانتقال لشاشة تسجيل الدخول
            _eventFlow.emit(UiEvent.SignUpSuccessWithVerification)

        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false)
            showError("فشل التسجيل: ${e.message}")
        }
    }

    private fun showError(message: String) {
        _state.value = _state.value.copy(isLoading = false, signUpError = message)
        viewModelScope.launch {
            delay(3000)
            _state.value = _state.value.copy(signUpError = null)
        }
    }

    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        data object SignUpSuccessWithVerification : UiEvent() // للانتقال لشاشة تسجيل الدخول
    }
}
