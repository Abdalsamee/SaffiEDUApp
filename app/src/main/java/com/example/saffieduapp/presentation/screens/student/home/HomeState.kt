package com.example.saffieduapp.presentation.screens.student.home

import com.example.saffieduapp.domain.model.FeaturedLesson
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.domain.model.UrgentTask

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val studentName: String = "",
    val studentGrade: String = "",
    val profileImageUrl: String = "",
    val searchQuery: String = "",
    val urgentTasks: List<UrgentTask> = emptyList(),
    val enrolledSubjects: List<Subject> = emptyList(),
    val featuredLessons: List<FeaturedLesson> = emptyList(),
    val error: String = "",
    val studentId: String = ""
)