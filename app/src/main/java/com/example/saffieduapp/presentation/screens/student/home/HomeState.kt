package com.example.saffieduapp.presentation.screens.student.home

import androidx.compose.ui.graphics.Color

// --- UI Models: هذه هي الكلاسات التي ستستخدمها الواجهة مباشرة ---

data class UrgentTaskUiModel(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val imageUrl: String,
    //val cardColor: Color,
    val startTime:String
)

data class EnrolledSubjectUiModel(
    val id: String,
    val name: String,
    val teacherName: String,
    val rating: Float,
    val imageUrl: String,
   // val cardColor: Color,
    val grade: String,
)

data class FeaturedLessonUiModel(
    val id: String,
    val title: String,
    val subject: String,
    val duration: String,
    val progress: Int,
    val imageUrl: String,
  //  val cardColor: Color
)

// --- State: هذا هو الكلاس الذي يمثل حالة الشاشة الكاملة ---

data class HomeState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val studentName: String = "",
    val studentGrade: String = "",
    val profileImageUrl: String = "",
    val searchQuery: String = "",
    val urgentTasks: List<UrgentTaskUiModel> = emptyList(),
    val enrolledSubjects: List<EnrolledSubjectUiModel> = emptyList(),
    val featuredLessons: List<FeaturedLessonUiModel> = emptyList(),
    val error: String? = null
)