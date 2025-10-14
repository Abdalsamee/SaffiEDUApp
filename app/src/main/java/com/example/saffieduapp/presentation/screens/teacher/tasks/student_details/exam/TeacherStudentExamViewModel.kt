package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherStudentExamViewModel : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentExamState(isLoading = true))
    val state: StateFlow<TeacherStudentExamState> = _state

    init {
        loadExamData()
    }

    private fun loadExamData() {
        viewModelScope.launch {
            delay(1000) // محاكاة تحميل البيانات من Firebase
            _state.value = TeacherStudentExamState(
                isLoading = false,
                studentName = "يزن عادل ظهير",
                studentImage = "https://randomuser.me/api/portraits/men/60.jpg",
                earnedScore = "15",
                totalScore = "20",
                answerStatus = "مكتملة",
                totalTime = "45 دقيقة",
                examStatus = "مستبعد",
                cheatingLogs = listOf(
                    "10:05 ص → خرج من التطبيق (تنبيه)",
                    "10:15 ص → أوقف الكاميرا",
                    "10:20 ص → عودة للامتحان"
                ),
                imageUrls = listOf(
                    "https://picsum.photos/200/300",
                    "https://picsum.photos/200/301",
                    "https://picsum.photos/200/302"
                ),
                videoUrl = "https://cdn-icons-png.flaticon.com/512/1384/1384060.png"
            )
        }
    }

    fun onSaveExamEvaluation() { /* لاحقاً: حفظ التقييم */ }

    fun onViewAnswersClick() { /* لاحقاً: عرض الإجابات */ }

    fun onImageClick(url: String) { /* لاحقاً: عرض الصورة داخل Dialog */ }

    fun onVideoClick() { /* لاحقاً: تشغيل الفيديو */ }
}
