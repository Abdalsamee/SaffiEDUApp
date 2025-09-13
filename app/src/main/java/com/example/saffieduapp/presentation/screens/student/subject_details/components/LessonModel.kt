package com.example.saffieduapp.presentation.screens.student.subject_details.components

import java.io.File


/**
 * A data class representing a PDF lesson.
 */
data class Lesson(
    val id: String,
    val title: String,
    val subTitle: String,
    val duration: Int,
    val videoFile: File? = null,  // ✅ ملف الفيديو بعد فك Base64
    val progress: Float = 0f
)