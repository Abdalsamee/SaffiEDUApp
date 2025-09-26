package com.example.saffieduapp.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.use_case.onboarding.GetOnboardingCompletedUseCase
import com.example.saffieduapp.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

                // 🔹 تشغيل الاستعلامات بالتوازي
                val teacherByUidDeferred = async {
                    firestore.collection("teachers")
                        .whereEqualTo("uid", uid)
                        .limit(1)
                        .get()
                        .await()
                }

                val teacherByEmailDeferred = async {
                    email?.let {
                        firestore.collection("teachers")
                            .whereEqualTo("email", it)
                            .limit(1)
                            .get()
                            .await()
                    }
                }

                val studentByUidDeferred = async {
                    firestore.collection("students")
                        .whereEqualTo("uid", uid)
                        .limit(1)
                        .get()
                        .await()
                }

                val studentByEmailDeferred = async {
                    email?.let {
                        firestore.collection("students")
                            .whereEqualTo("email", it)
                            .limit(1)
                            .get()
                            .await()
                    }
                }

                val teacherByUid = teacherByUidDeferred.await()
                val teacherByEmail = teacherByEmailDeferred.await()
                val studentByUid = studentByUidDeferred.await()
                val studentByEmail = studentByEmailDeferred.await()

                // 🔹 تحقق من المعلم أولاً
                if (!teacherByUid.isEmpty || (teacherByEmail != null && !teacherByEmail.isEmpty)) {
                    _startDestination.value = Routes.TEACHER_GRAPH
                    return@launch
                }

                // 🔹 تحقق من الطالب
                if (!studentByUid.isEmpty || (studentByEmail != null && !studentByEmail.isEmpty)) {
                    _startDestination.value = if (currentUser.isEmailVerified) {
                        Routes.MAIN_GRAPH
                    } else {
                        Routes.LOGIN_SCREEN
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
