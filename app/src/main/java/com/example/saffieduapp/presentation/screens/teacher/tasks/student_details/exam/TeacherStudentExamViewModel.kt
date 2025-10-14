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

    /**
     * 🔹 تحميل بيانات الطالب والاختبار (محاكاة Firebase)
     * لاحقًا: سيتم استبدال هذه الدالة بالاستدعاء الفعلي لـ Firestore.
     */
    private fun loadExamData() {
        viewModelScope.launch {
            try {
                delay(1000) // محاكاة تأخير الشبكة أو التحميل

                _state.value = TeacherStudentExamState(
                    isLoading = false,
                    studentName = "يزن عادل ظهير",
                    studentImageUrl = "https://randomuser.me/api/portraits/men/60.jpg",
                    earnedScore = 15,
                    totalScore = 20,
                    answerStatus = "مكتملة",
                    totalTimeMinutes = 45,
                    examStatus = ExamStatus.EXCLUDED,
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
                    videoUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"
                )

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "حدث خطأ أثناء تحميل البيانات") }
            }
        }
    }

    /**
     * 🔹 تحديث درجة الطالب أثناء الكتابة
     * يتم التحقق من إدخال أرقام صحيحة فقط
     */
    fun onScoreChange(newScore: String) {
        val numericValue = newScore.toIntOrNull() ?: 0
        _state.update { it.copy(earnedScore = numericValue) }
    }

    /**
     * 🔹 حفظ تقييم الطالب (لاحقًا سيتم ربطها بـ Firestore)
     */
    fun onSaveExamEvaluation() {
        viewModelScope.launch {
            println("✅ تم حفظ تقييم الطالب (${_state.value.studentName}) بنجاح.")
            // TODO: حفظ في Firestore عبر collection("exam_submissions")
        }
    }

    /**
     * 🔹 فتح شاشة عرض إجابات الطالب
     */
    fun onViewAnswersClick() {
        println("📄 عرض إجابات الطالب: ${_state.value.studentName}")
        // TODO: navController.navigate(Routes.TEACHER_STUDENT_EXAM_ANSWERS)
    }

    /**
     * 🔹 عند النقر على صورة مراقبة
     */
    fun onImageClick(url: String) {
        println("🖼️ عرض الصورة: $url")
        // TODO: فتح Dialog أو شاشة لعرض الصورة بالحجم الكامل
    }

    /**
     * 🔹 عند النقر على الفيديو
     */
    fun onVideoClick() {
        println("🎥 تشغيل الفيديو من الرابط: ${_state.value.videoUrl}")
        // TODO: تشغيل الفيديو باستخدام ExoPlayer أو External Viewer
    }
}
