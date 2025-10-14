package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherStudentExamViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentExamState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        // محاكاة جلب البيانات
        loadExamDetails()
    }

    private fun loadExamDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1000)

            _state.value = TeacherStudentExamState(
                isLoading = false,
                studentName = "يزن عادل ظهر",
                studentAvatarUrl = "https://picsum.photos/200",
                earnedScore = "15 من 20",
                answersStatus = "مكتملة",
                totalAttemptTime = "45 دقيقة",
                overallStatus = "مستبعد",
                cheatingLogs = listOf(
                    "10:05 ص → خرج من التطبيق (تنبيه)",
                    "10:15 ص → أوقف الكاميرا",
                    "10:20 ص → عودة للامتحان"
                ),
                photoShots = listOf(
                    "https://picsum.photos/seed/1/400",
                    "https://picsum.photos/seed/2/400",
                    "https://picsum.photos/seed/3/400"
                ),
                videoShots = listOf(
                    "https://samplelib.com/lib/preview/mp4/sample-5s.mp4" // مثال
                )
            )
        }
    }

    // لاحقاً: دوال حفظ التقييم/فتح الوسائط...إلخ
}
