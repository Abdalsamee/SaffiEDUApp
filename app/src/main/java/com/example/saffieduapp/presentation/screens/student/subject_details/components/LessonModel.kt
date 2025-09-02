package com.example.saffieduapp.presentation.screens.student.subject_details.components


/**
 * A data class representing a PDF lesson.
 */
data class Lesson(
    val id: Int,
    val title: String,
    val subTitle: String,
    val duration: Int,
    //val pagesCount: Int,
    val imageUrl: String,
    val progress: Float
)