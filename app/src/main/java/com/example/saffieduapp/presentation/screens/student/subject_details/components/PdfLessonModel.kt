package com.example.saffieduapp.presentation.screens.student.subject_details.components


data class PdfLesson(
    val id: Int,
    val title: String,
    val subTitle: String,
    val pagesCount: Int,
    val isRead: Boolean,
    val imageUrl: String
)