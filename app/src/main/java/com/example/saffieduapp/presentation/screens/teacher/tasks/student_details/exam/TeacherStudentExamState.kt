package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class TeacherStudentExamState(
    val isLoading: Boolean = false,

    // 🔹 بيانات الطالب
    val studentName: String = "",
    val studentImageUrl: String? = null,

    // 🔹 بيانات التقييم
    val earnedScore: Int = 0,
    val totalScore: Int = 0,
    val answerStatus: String = "",       // مثلاً "مكتملة" أو "غير مكتملة"
    val totalTimeMinutes: Int = 0,       // الوقت بالدقائق
    val examStatus: ExamStatus = ExamStatus.COMPLETED,

    // 🔹 بيانات المراقبة
    val cheatingLogs: List<String> = emptyList(),
    val imageUrls: List<String>? = null,
    val videoUrl: String? = null,

    // 🔹 إدارة الحالة
    val errorMessage: String? = null
)

enum class ExamStatus {
    COMPLETED,
    IN_PROGRESS,
    EXCLUDED
}
