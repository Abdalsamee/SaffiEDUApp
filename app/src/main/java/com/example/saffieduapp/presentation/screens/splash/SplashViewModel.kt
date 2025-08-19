package com.example.saffieduapp.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.use_case.onboarding.GetOnboardingCompletedUseCase
import com.example.saffieduapp.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkOnboardingAndLogin()
    }

    private fun checkOnboardingAndLogin() {
        viewModelScope.launch {
            val isOnboardingCompleted = getOnboardingCompletedUseCase().first()
            if (!isOnboardingCompleted) {
                _startDestination.value = Routes.ONBOARDING_SCREEN
                return@launch
            }

            val currentUser = auth.currentUser
            if (currentUser == null || !currentUser.isEmailVerified) {
                _startDestination.value = Routes.LOGIN_SCREEN
                return@launch
            }

            // ✅ نحاول تحديد الدور عبر uid ثم عبر email
            val uid = currentUser.uid
            val email = currentUser.email

            try {
                // هل هو طالب؟
                val studentByUid = firestore.collection("students")
                    .whereEqualTo("uid", uid)
                    .limit(1)
                    .get()
                    .await()

                val isStudent = !studentByUid.isEmpty || (
                        email != null && !firestore.collection("students")
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .await()
                            .isEmpty
                        )

                if (isStudent) {
                    _startDestination.value = Routes.MAIN_GRAPH
                    return@launch
                }

                // هل هو معلم؟
             /*   val teacherByUid = firestore.collection("teachers")
                    .whereEqualTo("uid", uid)
                    .limit(1)
                    .get()
                    .await()

                val isTeacher = !teacherByUid.isEmpty || (
                        email != null && !firestore.collection("teachers")
                            .whereEqualTo("email", email)
                            .limit(1)
                            .get()
                            .await()
                            .isEmpty
                        )

                _startDestination.value = if (isTeacher) {
                    Routes.TEACHER_HOME
                }*/ else {
                    // لم نعثر على دوره — نعيده لتسجيل الدخول
                    Routes.LOGIN_SCREEN
                }
            } catch (_: Exception) {
                _startDestination.value = Routes.LOGIN_SCREEN
            }
        }
    }
}
