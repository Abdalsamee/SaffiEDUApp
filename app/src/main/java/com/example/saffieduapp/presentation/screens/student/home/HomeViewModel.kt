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
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 15)
        )

        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )


        _state.value = _state.value.copy(
            studentName = "يزن عادل ضهير",
            studentGrade = "مصمم التطبيق Ui/Ux",
            profileImageUrl = "https://instagram.fgza2-5.fna.fbcdn.net/v/t51.2885-19/519497419_17974326737899750_3401532740011521622_n.jpg?efg=eyJ2ZW5jb2RlX3RhZyI6InByb2ZpbGVfcGljLmRqYW5nby4xMDgwLmMyIn0&_nc_ht=instagram.fgza2-5.fna.fbcdn.net&_nc_cat=110&_nc_oc=Q6cZ2QEAjYEcIt3ibYUz-_ZxzPQ6LWBAHB0LVbTmbyydG8aFuUFgzui5xS3BbPGcDHa2gWI&_nc_ohc=9caozL-qv6UQ7kNvwHa8jHJ&_nc_gid=2X9fZrOnRU9DXAmfrxeNXQ&edm=AP4sbd4BAAAA&ccb=7-5&oh=00_AfUiJ45ROL4CzXl8rLxDR_Fp6xlbE1gVFi9fz1YrCeVIpw&oe=68964CBC&_nc_sid=7a9f4b",
            urgentTasks = urgentTasksList,
            enrolledSubjects = subjectsList,
            featuredLessons = lessonsList
        )
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}