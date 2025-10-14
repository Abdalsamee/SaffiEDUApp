package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeacherStudentExamViewModel : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentExamState(isLoading = true))
    val state: StateFlow<TeacherStudentExamState> = _state

    init {
        loadExamData()
    }

    // 🔹 محاكاة تحميل بيانات الطالب والاختبار من Firebase
    private fun loadExamData() {
        viewModelScope.launch {
            delay(1000) // محاكاة تأخير الشبكة
            _state.value = TeacherStudentExamState(
                isLoading = false,
                studentName = "يزن عادل ظهير",
                studentImageUrl = "https://randomuser.me/api/portraits/men/60.jpg",
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

    // 🔹 تعديل العلامة أثناء الكتابة
    fun onScoreChange(newScore: String) {
        _state.update { it.copy(earnedScore = newScore) }
    }

    // 🔹 حفظ التقييم بعد إدخال الدرجة أو التعليق
    fun onSaveExamEvaluation() {
        // لاحقاً: حفظ في Firestore
        println("✅ تم حفظ تقييم الطالب (${_state.value.studentName}) بنجاح.")
    }

    // 🔹 عرض إجابات الطالب
    fun onViewAnswersClick() {
        // لاحقاً: الانتقال إلى شاشة عرض الإجابات
        println("📄 عرض إجابات الطالب: ${_state.value.studentName}")
    }

    // 🔹 عرض صورة المراقبة داخل Dialog
    fun onImageClick(url: String) {
        println("🖼️ عرض الصورة: $url")
    }

    // 🔹 تشغيل الفيديو داخل عارض خارجي
    fun onVideoClick() {
        println("🎥 تشغيل الفيديو من الرابط: ${_state.value.videoUrl}")
    }
}
