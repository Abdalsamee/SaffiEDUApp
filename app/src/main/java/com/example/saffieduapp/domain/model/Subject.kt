package com.example.saffieduapp.domain.model


data class Subject(
    val id: String,
    val name: String,
    val teacherName: String = "",
    val grade: String,
    val rating: Float = 0f,
    val imageUrl: String = "",
    val totalLessons: Int = 0,
)

