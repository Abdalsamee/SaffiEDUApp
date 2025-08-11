package com.example.saffieduapp.presentation.screens.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.FeaturedLesson
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.domain.model.UrgentTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(1500)
            fetchAndProcessData()
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1000) // محاكاة تحميل أولي
            fetchAndProcessData()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun fetchAndProcessData() {
        // --- بيانات مؤقتة (Dummy Data) ---
        // يتم الآن استخدام موديلات البيانات الأساسية مباشرة من طبقة الـ domain
        val urgentTasksList = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )

        val subjectsList = listOf(
            Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 12),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 20),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15),

        )

        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 100, "")
        )


        _state.value = _state.value.copy(
            studentName = "م.عبدالسميع النجار",
            studentGrade = "مطور ومهندس التطبيق",
            profileImageUrl = "https://2u.pw/rtUBp",
            urgentTasks = urgentTasksList,
            enrolledSubjects = subjectsList,
            featuredLessons = lessonsList
        )
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}