package com.example.saffieduapp.presentation.screens.student.subject_details.components

import java.io.File

data class PdfLesson(
    val id: String,
    val title: String,
    val subTitle: String,
    val pagesCount: Int,
    val isRead: Boolean,
    val imageUrl: String = "",
    val pdfUrl: String = "",
    val pdfFile: File? = null,     // ✅ ملف PDF محلي بعد فك الـ Base64
    val base64: String? = null     // ✅ نص Base64 الأصلي
)
