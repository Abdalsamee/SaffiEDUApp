package com.example.saffieduapp.presentation.screens.student.exam_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentExamResultViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(StudentExamResultState(isLoading = true))
    val state: StateFlow<StudentExamResultState> = _state

    /**
     * 🔹 تحميل بيانات النتيجة
     * لاحقاً: سيتم ربطها مع Firestore حسب examId و studentId
     */
    fun loadExamResult(examId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(1000) // محاكاة تأخير الشبكة (حالياً فقط)

            // 🔹 بيانات مؤقتة لمحاكاة النتيجة
            _state.update {
                it.copy(
                    isLoading = false,
                    examTitle = "اختبار الوحدة الثانية",
                    subjectName = "مادة التربية الإسلامية",
                    totalScore = "15",
                    earnedScore = "15",
                    isGraded = true
                )
            }
        }
    }
}
