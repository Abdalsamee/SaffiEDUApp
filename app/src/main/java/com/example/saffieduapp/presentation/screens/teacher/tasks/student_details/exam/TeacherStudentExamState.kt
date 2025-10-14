package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

data class TeacherStudentExamState(
    val isLoading: Boolean = false,

    // 🔹 بيانات الطالب
    val studentName: String = "",
    val studentImageUrl: String = "", // ✅ تعديل الاسم ليتبع نفس النمط في باقي المشروع

    // 🔹 بيانات التقييم
    val earnedScore: String = "",
    val totalScore: String = "",
    val answerStatus: String = "",
    val totalTime: String = "",
    val examStatus: String = "",

    // 🔹 بيانات المراقبة
    val cheatingLogs: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String = "",

    // 🔹 إدارة الحالة
    val errorMessage: String? = null
)
