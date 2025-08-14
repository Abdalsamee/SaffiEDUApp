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

    // حالة الشاشة - تتغير مع تفاعل المستخدم أو تحديث البيانات
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    // لتبادل الأحداث بين ViewModel و UI (مثل النجاح أو الرسائل)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // التعامل مع الأحداث التي تصدر من الشاشة (مثل تغييرات النصوص أو النقر على زر التسجيل)
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
            is SignUpEvent.SignUpClicked -> viewModelScope.launch { signUpUser() } // بدأ عملية التسجيل عند النقر على الزر
        }
    }

    // دالة التسجيل الرئيسية: التحقق من البيانات ثم محاولة التسجيل في Firebase
    private suspend fun signUpUser() {
        val currentState = _state.value

        // التحقق من صحة البيانات المدخلة، إذا فشل تظهر رسالة خطأ وتوقف العملية
        if (!validateInputs(currentState)) return

        // تحديث الحالة إلى تحميل أثناء تنفيذ التسجيل
        _state.value = _state.value.copy(isLoading = true, signUpError = null)

        try {
            delay(3000) // تأخير محاكاة لتخفيف ضغط الشبكة أو انتظار استجابة Firebase

            // التحقق إذا كان رقم الهوية موجود مسبقاً في قاعدة البيانات
            if (authRepository.isIdNumberExists(currentState.idNumber)) {
                _state.value = _state.value.copy(isLoading = false)
                showError("رقم الهوية مستخدم مسبقًا")
                return
            }

            // إنشاء حساب Firebase Authentication بالبريد وكلمة السر
            authRepository.createUserWithEmailAndPassword(
                email = currentState.email,
                password = currentState.password
            )

            // تخزين باقي بيانات المستخدم في Firestore بدون كلمة المرور
            authRepository.registerUserData(
                idNumber = currentState.idNumber,
                fullName = currentState.fullName,
                email = currentState.email,
                grade = currentState.selectedGrade
            )

            // تحديث الحالة بعد نجاح التسجيل
            _state.value = _state.value.copy(isLoading = false)

            // إرسال حدث نجاح للواجهة لبدء الانتقال إلى شاشة تسجيل الدخول
            _eventFlow.emit(UiEvent.SignUpSuccessWithVerification)

        } catch (e: Exception) {
            // في حالة وجود خطأ، عرض رسالة للمستخدم
            _state.value = _state.value.copy(isLoading = false)
            showError("فشل التسجيل: ${e.message}")
        }
    }

    // دالة مساعدة للتحقق من صحة كل الحقول المطلوبة
    private fun validateInputs(state: SignUpState): Boolean {
        if (state.fullName.isBlank() ||
            state.idNumber.isBlank() ||
            state.email.isBlank() ||
            state.password.isBlank() ||
            state.confirmPassword.isBlank() ||
            state.selectedGrade.isBlank()
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

        if (state.password.length < 6) {
            showError("كلمة المرور يجب أن تكون 6 أحرف على الأقل")
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

    // دالة عرض رسالة الخطأ في الحالة مع إزالة الرسالة بعد 3 ثواني تلقائياً
    private fun showError(message: String) {
        _state.value = _state.value.copy(signUpError = message)
        viewModelScope.launch {
            delay(3000)
            _state.value = _state.value.copy(signUpError = null)
        }
    }

    // أحداث خاصة بالواجهة (يمكن إضافتها لاحقًا حسب الحاجة)
    sealed class UiEvent {
        data class ShowMessage(val message: String) : UiEvent()
        object SignUpSuccessWithVerification : UiEvent() // عند نجاح التسجيل
    }
}
