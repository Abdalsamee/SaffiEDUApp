package com.example.saffieduapp.presentation.screens.student.subject_details.components

import android.R
import android.R.bool

data class PdfLesson(
    val id: String,
    val title: String,
    val subTitle: String,
    val pagesCount: Int,
    val isRead: Boolean,
    val imageUrl: String = "",
    val pdfUrl: String = "",
)
