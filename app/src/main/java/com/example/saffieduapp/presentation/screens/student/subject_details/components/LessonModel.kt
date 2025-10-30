package com.example.saffieduapp.presentation.screens.student.subject_details.components



data class Lesson(
    val id: String? = null,
    val subjectId: String? = null,
    val className: String? = null,
    val title: String? = null,
    val publicationDate: String = "",
    val subTitle: String? = null,
    val duration: Int? = 0,
    val description: String? = null,
    val pdfUrl: String? = null,
    val videoUrl: String,
    val pagesCount: Int? = 0,
    val notificationStatus: String? = null,
    val progress: Int = 0, // أضف هذا
)
