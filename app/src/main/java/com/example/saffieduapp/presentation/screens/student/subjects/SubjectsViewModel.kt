package com.example.saffieduapp.presentation.screens.student.subjects

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.domain.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "",12),
            Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "",11),
            Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "",21)
        )
        _state.value = SubjectsState(isLoading = false, subject = subjectsList)
    }
}