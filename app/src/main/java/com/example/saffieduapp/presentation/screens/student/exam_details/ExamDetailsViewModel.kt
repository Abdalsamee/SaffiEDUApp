package com.example.saffieduapp.presentation.screens.student.exam_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ExamDetailsState())
    val state = _state.asStateFlow()

    init {
        val examId = savedStateHandle.get<String>("examId")
        if (examId != null) {
            loadExamDetails(examId)
        }
    }

    private fun loadExamDetails(id: String) {
        // بيانات وهمية مؤقتة
        val dummyDetails = ExamDetails(
            id = id,
            title = "اختبار الوحدة الثانية",
            subjectName = "التربية الإسلامية",
            teacherName = "أ. فراس شعبان",
            imageUrl = "",
            date = "24 / 8 / 2025، الثلاثاء",
            startTime = "02:30 pm",
            endTime = "03:00 pm",
            durationInMinutes = 30,
            questionCount = 30,
            status = "متاح"
        )

        _state.value = ExamDetailsState(isLoading = false, examDetails = dummyDetails)
    }
}