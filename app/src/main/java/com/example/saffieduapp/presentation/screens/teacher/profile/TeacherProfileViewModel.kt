package com.example.saffieduapp.presentation.screens.teacher.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherProfileState(isLoading = true))
    val state: StateFlow<TeacherProfileState> = _state

    init {
        loadMockTeacherProfile()
    }

    /**
     * 🧩 تحميل بيانات وهمية بدلًا من Firestore
     * تحاكي العملية الحقيقية لعرض شاشة المعلم أثناء التطوير أو بدون اتصال Firebase.
     */
    private fun loadMockTeacherProfile() {
        viewModelScope.launch {
            // 🔹 تأخير بسيط لمحاكاة التحميل
            delay(1500)

            // 🔹 تعبئة البيانات الوهمية
            _state.value = TeacherProfileState(
                isLoading = false,
                fullName = " عبدالسميع النجار",
                email = "abdalsamee.teacher@gmail.com",
                nationalId = "409115011",
                subject = "الرياضيات",
                classesCount = 5,
                profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/demo-app/o/teacher_profile.png?alt=media"
            )
        }
    }

    /**
     * 🚪 تسجيل الخروج (وهمي أيضًا)
     */
    fun logout() {
        viewModelScope.launch {
            _state.value = TeacherProfileState(
                isLoading = false,
                fullName = "",
                email = "",
                nationalId = "",
                subject = "",
                classesCount = 0,
                profileImageUrl = null,
                error = "تم تسجيل الخروج بنجاح ✅"
            )
        }
    }
}
