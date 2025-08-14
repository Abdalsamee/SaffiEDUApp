package com.example.saffieduapp.presentation.screens.student.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SubjectsState())
    val state = _state.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        val subjectsList = listOf(
            Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 0f, "", 12),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 0f, "", 21),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 0f, "", 11),
        )

        _state.value = SubjectsState(isLoading = false, subject = subjectsList)
    }
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            delay(1500) // محاكاة تأخير الشبكة
            loadSubjects() // إعادة تحميل البيانات
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    // تم إضافة الدالة المفقودة هنا
    fun updateRating(subjectId: String, newRating: Int) {
        println("Subject with ID $subjectId was rated with $newRating stars.")

        // (اختياري) تحديث التقييم في الواجهة مباشرة للمعاينة
        val currentSubjects = _state.value.subject.toMutableList()
        val subjectIndex = currentSubjects.indexOfFirst { it.id == subjectId }
        if (subjectIndex != -1) {
            val updatedSubject = currentSubjects[subjectIndex].copy(rating = newRating.toFloat())
            currentSubjects[subjectIndex] = updatedSubject
            _state.value = _state.value.copy(subject = currentSubjects)
        }
    }
}