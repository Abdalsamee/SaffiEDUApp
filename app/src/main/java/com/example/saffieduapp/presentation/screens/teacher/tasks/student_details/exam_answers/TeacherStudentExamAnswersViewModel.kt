package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeacherStudentExamAnswersViewModel : ViewModel() {

    private val _state = MutableStateFlow(TeacherStudentExamAnswersState(isLoading = true))
    val state: StateFlow<TeacherStudentExamAnswersState> = _state

    init {
        loadStudentAnswers()
    }

    private fun loadStudentAnswers() {
        viewModelScope.launch {
            delay(800) // محاكاة التحميل من Firebase
            _state.value = TeacherStudentExamAnswersState(
                isLoading = false,
                answers = listOf(
                    StudentAnswer(
                        questionId = "q1",
                        questionText = "نص السؤال الأول",
                        answerText = "نص الإجابة التي اختارها الطالب",
                        questionType = QuestionType.SINGLE_CHOICE,
                        maxScore = 5
                    ),
                    StudentAnswer(
                        questionId = "q2",
                        questionText = "نص السؤال الثاني",
                        answerText = "نص الإجابة التي اختارها الطالب",
                        questionType = QuestionType.TRUE_FALSE,
                        maxScore = 3
                    ),
                    StudentAnswer(
                        questionId = "q3",
                        questionText = "نص السؤال الثالث",
                        answerText = "نص الإجابة المقالية التي كتبها الطالب ....",
                        questionType = QuestionType.ESSAY,
                        maxScore = 10
                    )
                )
            )
        }
    }

    fun onScoreSelected(questionId: String, newScore: Int) {
        _state.update { current ->
            val updatedAnswers = current.answers.map {
                if (it.questionId == questionId) it.copy(assignedScore = newScore) else it
            }
            current.copy(
                answers = updatedAnswers,
                totalScore = updatedAnswers.sumOf { it.assignedScore ?: 0 }
            )
        }
    }

    fun onSaveGrades() {
        println("✅ تم حفظ الدرجات بنجاح - المجموع الكلي: ${_state.value.totalScore}")
    }
}
