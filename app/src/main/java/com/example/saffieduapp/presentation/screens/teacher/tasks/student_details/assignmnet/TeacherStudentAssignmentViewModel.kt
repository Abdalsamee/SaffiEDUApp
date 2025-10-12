package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.assignmnet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherStudentAssignmentViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentAssignmentState())
    val state = _state.asStateFlow()

    init {
        // نحاكي تحميل بيانات الطالب عند فتح الشاشة
        loadStudentAssignmentDetails()
    }

    private fun loadStudentAssignmentDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            delay(1200) // محاكاة وقت التحميل

            _state.value = _state.value.copy(
                isLoading = false,
                studentName = "محمد أحمد",
                studentClass = "الصف السادس",
                deliveryStatus = "تم التسليم",
                submittedFiles = listOf(
                    SubmittedFile(
                        fileName = "واجب اللغة العربية.pdf",
                        fileUrl = "https://example.com/file1.pdf",
                        isImage = false
                    ),
                    SubmittedFile(
                        fileName = "صورة من الواجب",
                        fileUrl = "https://example.com/image1.jpg",
                        isImage = true
                    )
                ),
                grade = "",
                comment = ""
            )
        }
    }

    fun onGradeChange(value: String) {
        _state.value = _state.value.copy(grade = value)
    }

    fun onCommentChange(value: String) {
        _state.value = _state.value.copy(comment = value)
    }

    fun onSaveEvaluation() {
        viewModelScope.launch {
            val current = _state.value
            println("✅ تم حفظ تقييم الطالب ${current.studentName}")
            println("الدرجة: ${current.grade}")
            println("التعليق: ${current.comment}")

            // لاحقاً: ربط فعلي مع Firestore لحفظ التقييم
        }
    }
}
