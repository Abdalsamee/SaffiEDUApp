package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

data class TeacherStudentExamState(
    val isLoading: Boolean = false,

    // معلومات الطالب
    val studentName: String = "",
    val studentAvatarUrl: String? = null,

    // معلومات النتيجة/المحاولة
    val earnedScore: String = "",       // مثال: "15 من 20"
    val answersStatus: String = "",     // مثال: "مكتملة" / "غير مكتملة"
    val totalAttemptTime: String = "",  // مثال: "45 دقيقة"
    val overallStatus: String = "",     // مثال: "نشط" أو "مستبعد"

    // محاولات الغش
    val cheatingLogs: List<String> = emptyList(), // مثال: ["10:05 ص - خرج من التطبيق (تنبيه)", "10:15 ص - أوقف الكاميرا"]

    // الوسائط
    val photoShots: List<String> = emptyList(),   // روابط صور المراقبة
    val videoShots: List<String> = emptyList(),   // روابط فيديو المراقبة

    val errorMessage: String? = null
)
