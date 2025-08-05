package com.example.saffieduapp.domain.model

data class FeaturedLesson(
    val id: String,
    val title: String,
    val subject: String,
    val duration: String,
    val progress: Int,
    val imageUrl: String
)