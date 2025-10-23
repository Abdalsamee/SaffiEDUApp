package com.example.saffieduapp.presentation.screens.student.exam_result

data class StudentExamResultState(
    val isLoading: Boolean = true,

    // 🔹 معلومات الاختبار
    val examTitle: String = "",
    val subjectName: String = "",

    // 🔹 نتيجة الطالب
    val totalScore: String = "",
    val earnedScore: String = "",

    // 🔹 هل تم التقييم من قبل المعلم
    val isGraded: Boolean = false,

    // هل تسمح بإظهار النتيجة فوراً
    val showResultsImmediately: Boolean = false,

    // 🔹 في حال حدوث خطأ
    val errorMessage: String? = null
)
