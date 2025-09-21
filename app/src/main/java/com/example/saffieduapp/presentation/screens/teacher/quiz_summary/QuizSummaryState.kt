package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

// سنحتاج إلى تعريف موديل عام للسؤال لاحقًا
// مؤقتًا، سنستخدم String

data class QuizSummaryState(
    val examTitle: String = "عنوان الاختبار",
    val questions: List<String> = emptyList(),
    val isDeleting: Boolean = false // لتتبع حالة الحذف
)