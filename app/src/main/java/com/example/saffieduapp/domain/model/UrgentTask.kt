package com.example.saffieduapp.domain.model

data class UrgentTask(
    val id: String = "",
    val examType: String = "",
    val endDate: String = "",
    val examStartTime: String = "",
    val subjectName: String = "",
    val imageUrl: String?
)