package com.example.saffieduapp.presentation.screens.student.home

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.theme.getSubjectColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


data class UrgentTask(val id: String, val title: String, val subject: String, val dueDate: String, val startTime: String, val imageUrl: String)
data class EnrolledSubject(val id: String, val name: String, val teacherName: String,val grade: String, val rating: Float, val isFavorite: Boolean, val imageUrl: String ,)
data class FeaturedLesson(val id: String, val title: String, val subject: String, val duration: String, val progress: Int, val imageUrl: String)


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadDummyData()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }

    private fun loadDummyData() {
        // 1. Raw data (as if from a repository)
        val rawTasks = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )
        val rawSubjects = listOf(
            EnrolledSubject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, true, ""),
            EnrolledSubject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, true, ""),
            EnrolledSubject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, false, "")
        )
        val rawLessons = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        // 2. Map raw data to UI models, adding the color logic
        val tasksWithColor = rawTasks.map {
            UrgentTaskUiModel(
                id = it.id, title = it.title, subject = it.subject, dueDate = it.dueDate,
                imageUrl = it.imageUrl, cardColor = getSubjectColor(it.subject)
            )
        }
        val subjectsWithColor = rawSubjects.map {
            EnrolledSubjectUiModel(
                id = it.id, name = it.name, teacherName = it.teacherName, rating = it.rating,
                imageUrl = it.imageUrl, cardColor = getSubjectColor(it.name), grade ="الصف العاشر"
            )
        }
        val lessonsWithColor = rawLessons.map {
            FeaturedLessonUiModel(
                id = it.id, title = it.title, subject = it.subject, progress = it.progress,
                imageUrl = it.imageUrl, cardColor = getSubjectColor(it.subject)
            )
        }

        // 3. Update the state with the final, colored UI models
        _state.value = _state.value.copy(
            isLoading = false,
            studentName = "محمد خالد",
            studentGrade = "الصف العاشر",
            profileImageUrl = "",
            urgentTasks = tasksWithColor,
            enrolledSubjects = subjectsWithColor,
            featuredLessons = lessonsWithColor
        )
    }
}