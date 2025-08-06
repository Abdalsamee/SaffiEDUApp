package com.example.saffieduapp.domain.model

data class Subject(
    val id: String,
    val name: String,
    val teacherName: String,
    val grade: String,
    val rating: Float,
    val imageUrl: String,
    val lessonCount: Int
)