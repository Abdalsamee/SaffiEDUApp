package com.example.saffieduapp.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.local.preferences.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    sealed class UiEvent {
        data class LoginSuccess(val role: String) : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadSavedCredentials()
    }

    private fun loadSavedCredentials() {
        viewModelScope.launch {
            val (savedId, savedPassword) = preferencesManager.getCredentials().first()
            savedId?.let { id ->
                _uiState.value = _uiState.value.copy(
                    id = id,
                    rememberMe = true
                )
            }
            savedPassword?.let { password ->
                _uiState.value = _uiState.value.copy(
                    password = password,
                    rememberMe = true
                )
            }
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.IdChanged -> _uiState.value = _uiState.value.copy(id = event.id)
            is LoginEvent.PasswordChanged -> _uiState.value = _uiState.value.copy(password = event.password)
            is LoginEvent.RememberMeChanged -> _uiState.value = _uiState.value.copy(rememberMe = event.remember)
            is LoginEvent.TogglePasswordVisibility -> _uiState.value =
                _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
            is LoginEvent.LoginClicked -> loginUser()
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val id = _uiState.value.id.trim()
            val password = _uiState.value.password.trim()
            val rememberMe = _uiState.value.rememberMe

            if (id.isEmpty() || password.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "يرجى إدخال جميع البيانات"
                )
                return@launch
            }

            try {
                // حفظ بيانات "تذكرني"
                if (rememberMe) preferencesManager.saveCredentials(id, password)
                else preferencesManager.clearCredentials()

                // أولاً، تحقق من المعلمين
                val teacherSnapshot = firestore.collection("teachers").document(id).get().await()
                if (teacherSnapshot.exists()) {
                    val email = teacherSnapshot.getString("email") ?: run {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "لا يوجد بريد إلكتروني مرتبط بحساب المعلم"
                        )
                        return@launch
                    }
                    // تسجيل الدخول بدون تحقق البريد للمعلمين
                    auth.signInWithEmailAndPassword(email, password).await()
                    _eventFlow.emit(UiEvent.LoginSuccess("teacher"))
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }

                // إذا لم يكن معلم، تحقق من الطلاب
                val studentSnapshot = firestore.collection("students").document(id).get().await()
                if (!studentSnapshot.exists()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "رقم الهوية غير مسجل"
                    )
                    return@launch
                }

                val email = studentSnapshot.getString("email") ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "لا يوجد بريد إلكتروني مرتبط بهذا الحساب"
                    )
                    return@launch
                }

                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "فشل تسجيل الدخول: بيانات غير صحيحة"
                    )
                    return@launch
                }

                // تحقق من البريد للطلاب فقط
                if (!user.isEmailVerified) {
                    auth.signOut()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "يجب تفعيل البريد الإلكتروني أولاً"
                    )
                    return@launch
                }

                _eventFlow.emit(UiEvent.LoginSuccess("student"))
                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                    is com.google.firebase.FirebaseNetworkException -> "خطأ في الاتصال بالإنترنت"
                    else -> "حدث خطأ غير متوقع: ${e.localizedMessage}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}
