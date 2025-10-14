package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers

data class StudentAnswer(
    val questionId: String,
    val questionText: String,
    val answerText: String,
    val questionType: QuestionType,
    val maxScore: Int,
    val assignedScore: Int? = null
)

enum class QuestionType {
    SINGLE_CHOICE,
    MULTI_CHOICE,
    TRUE_FALSE,
    ESSAY
}

data class TeacherStudentExamAnswersState(
    val isLoading: Boolean = false,
    val answers: List<StudentAnswer> = emptyList(),
    val totalScore: Int = 0
)
