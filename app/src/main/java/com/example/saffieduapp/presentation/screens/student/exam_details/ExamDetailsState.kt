package com.example.saffieduapp.presentation.screens.student.exam_details

// ١. تعريف موديل تفاصيل الاختبار
data class ExamDetails(
    val id: String,
    val title: String,
    val subjectName: String,
    val teacherName: String,
    val imageUrl: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationInMinutes: Int,
    val questionCount: Int,
    val status: String // مثال: "متاح"
)

data class ExamDetailsState(
    val isLoading: Boolean = true,
    val examDetails: ExamDetails? = null,
    val error: String? = null
)