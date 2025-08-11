package com.example.saffieduapp.presentation.screens.student.subject_details

import androidx.lifecycle.SavedStateHandle
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
class SubjectDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectDetailsState())
    val state = _state.asStateFlow()

    // اجعل subjectId إلزاميًّا من وسيطات التنقل
    private val subjectId: String = checkNotNull(savedStateHandle["subjectId"]) {
        "subjectId is required in nav arguments"
    }

    init {
        load()
    }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // محاكاة جلب بيانات — استبدل لاحقًا بمناداة Repository
            delay(400)

            val allSubjects = listOf(
                Subject("s1", "اللغة العربية", "خالد عبدالله", "الصف العاشر", 4.5f, "", 1),
                Subject("s2", "التربية الإسلامية", "فراس شعبان", "الصف العاشر", 4.5f, "", 2),
                Subject("s3", "رياضيات", "عبدالسميع النجار", "الصف العاشر", 1.5f, "", 3)
            )

            val subject = allSubjects.find { it.id == subjectId }
            _state.value = _state.value.copy(isLoading = false, subject = subject)
        }
    }
}
