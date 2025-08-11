package com.example.saffieduapp.presentation.screens.student.subject_details

import com.example.saffieduapp.domain.model.Subject

data class SubjectDetailsState(
    val isLoading: Boolean = true,
    val subject: Subject? = null
)