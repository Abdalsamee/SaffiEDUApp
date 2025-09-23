package com.example.saffieduapp.presentation.screens.student.subjects

import com.example.saffieduapp.domain.model.Subject

data class SubjectsState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val subject: List<Subject> = emptyList(),
    val error: String
)