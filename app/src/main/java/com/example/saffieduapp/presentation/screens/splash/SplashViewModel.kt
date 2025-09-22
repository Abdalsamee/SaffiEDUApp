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
            try {
                val isOnboardingCompleted = getOnboardingCompletedUseCase().first()
                if (!isOnboardingCompleted) {
                    _startDestination.value = Routes.ONBOARDING_SCREEN
                    return@launch
                }

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _startDestination.value = Routes.LOGIN_SCREEN
                    return@launch
                }

                val uid = currentUser.uid
                val email = currentUser.email

                // ✅ التحقق أولاً من المعلم
                val teacherQuery = firestore.collection("teachers")
                    .whereEqualTo("uid", uid)
                    .limit(1)
                    .get()
                    .await()

                val teacherByEmail = if (email != null) {
                    firestore.collection("teachers")
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .await()
                } else null

                if (!teacherQuery.isEmpty || (teacherByEmail != null && !teacherByEmail.isEmpty)) {
                    // 🔹 المعلم لا يحتاج تحقق من البريد
                    _startDestination.value = Routes.TEACHER_GRAPH
                    return@launch
                }

                // ✅ التحقق ثانياً من الطالب
                val studentQuery = firestore.collection("students")
                    .whereEqualTo("uid", uid)
                    .limit(1)
                    .get()
                    .await()

                val studentByEmail = if (email != null) {
                    firestore.collection("students")
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .await()
                } else null

                if (!studentQuery.isEmpty || (studentByEmail != null && !studentByEmail.isEmpty)) {
                    // 🔹 الطالب لازم يكون مفعل البريد
                    if (currentUser.isEmailVerified) {
                        _startDestination.value = Routes.MAIN_GRAPH
                    } else {
                        _startDestination.value = Routes.LOGIN_SCREEN
                    }
                    return@launch
                }

                // 🔹 إذا لم يكن طالب ولا معلم → Login
                _startDestination.value = Routes.LOGIN_SCREEN

            } catch (_: Exception) {
                _startDestination.value = Routes.LOGIN_SCREEN
            }
        }
    }
}
