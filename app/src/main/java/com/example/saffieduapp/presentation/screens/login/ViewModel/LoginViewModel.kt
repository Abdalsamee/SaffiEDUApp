package com.example.saffieduapp.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    sealed class UiEvent {
        object LoginSuccess : UiEvent()
    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.IdChanged -> {
                _uiState.value = _uiState.value.copy(id = event.id)
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }
            is LoginEvent.RememberMeChanged -> {
                _uiState.value = _uiState.value.copy(rememberMe = event.remember)
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            is LoginEvent.LoginClicked -> {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val id = _uiState.value.id.trim()
            val password = _uiState.value.password.trim()

            // ✅ فحص الحقول الفارغة
            if (id.isEmpty() || password.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "يرجى إدخال جميع البيانات"
                )
                return@launch
            }

            // ✅ فحص طول كلمة المرور
            if (password.length < 6) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "كلمة المرور يجب أن تكون 6 خانات أو أكثر"
                )
                return@launch
            }

            try {
                // البحث عن المستخدم في Firestore باستخدام رقم الهوية
                val snapshot = firestore.collection("users")
                    .document(id)
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "رقم الهوية غير موجود"
                    )
                    return@launch
                }

                val email = snapshot.getString("email") ?: ""
                if (email.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "لا يوجد بريد إلكتروني مرتبط برقم الهوية"
                    )
                    return@launch
                }

                // تسجيل الدخول عبر Firebase Authentication
                val result = auth.signInWithEmailAndPassword(email, password).await()

                val user = result.user
                if (user != null) {
                    if (user.isEmailVerified) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _eventFlow.emit(UiEvent.LoginSuccess)
                    } else {
                        auth.signOut()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "يرجى تفعيل البريد الإلكتروني قبل تسجيل الدخول"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "حدث خطأ غير متوقع أثناء تسجيل الدخول"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "فشل تسجيل الدخول"
                )
            }
        }
    }
}
