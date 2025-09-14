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
    val progress: Float = 0f,
    val videoUrl: String
)