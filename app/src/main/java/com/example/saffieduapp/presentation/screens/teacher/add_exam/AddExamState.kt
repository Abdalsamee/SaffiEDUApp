package com.example.saffieduapp.presentation.screens.teacher.add_exam

data class AddExamState(
    val selectedClass: String = "",
    val examTitle: String = "",
    val examType: String = "",
    val examDate: String = "",
    val examStartTime: String = "",
    val examTime: String = "",
    val randomQuestions: Boolean = false,
    val showResultsImmediately: Boolean = false,
    val isSaving: Boolean = false,
    val success: Boolean? = null,
    val isDraftSaved: Boolean = false
)