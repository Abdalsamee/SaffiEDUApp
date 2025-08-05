package com.example.saffieduapp.domain.model

data class UrgentTask(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val startTime: String,
    val imageUrl: String
)