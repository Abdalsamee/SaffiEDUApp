package com.example.saffieduapp.presentation.screens.student.assignment_result

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
class StudentAssignmentResultViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(StudentAssignmentResultState(isLoading = true))
    val state: StateFlow<StudentAssignmentResultState> = _state

    /**
     * 🔹 هذه الدالة تُستدعى عند التنقل إلى الواجهة مع معرف الواجب
     * الهدف منها تحميل التقييم من Firebase لاحقاً بناءً على الـ assignmentId
     */
    fun loadResultData(assignmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(1000) // محاكاة تأخير الشبكة (اختياري حالياً)

            // 🔹 لاحقاً سيتم هنا استبدال البيانات الثابتة بقراءة من Firestore
            _state.update {
                it.copy(
                    isLoading = false,
                    assignmentTitle = "واجب اللغة العربية",
                    studentName = "فتح عبد السميع النجار",
                    files = listOf(
                        "pdf.120211726 واجب اللغة العربية",
                        "pdf.123 واجب اللغة العربية"
                    ),
                    grade = "10 / 10",
                    comment = "حل رائع جدًا 🌟",
                    errorMessage = null
                )
            }
        }
    }
}
