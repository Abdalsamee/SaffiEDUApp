package com.example.saffieduapp.presentation.screens.student.assignment_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentDetailsState())
    val state = _state.asStateFlow()

    init {
        val assignmentId = savedStateHandle.get<String>("assignmentId")
        if (assignmentId != null) {
            loadAssignmentDetails(assignmentId)
        }
    }

    private fun loadAssignmentDetails(id: String) {
        // بيانات وهمية مؤقتة
        val dummyDetails = AssignmentDetails(
            id = id,
            title = "النحو والصرف",
            description = "هذا الفيديو يشرح قوانين الكسور بشكل مبسط. هذا الفيديو يشرح قوانين الكسور بشكل مبسط.",
            imageUrl = null, // لا يوجد صورة في هذا المثال
            subjectName = "اللغة العربية",
            teacherName = "أ. طاهر زياد قديح",
            dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
            remainingTime = "متبقي 10 أيام"
        )

        _state.value = AssignmentDetailsState(isLoading = false, assignmentDetails = dummyDetails)
    }
}