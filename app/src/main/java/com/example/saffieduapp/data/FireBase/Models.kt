package com.example.saffieduapp.data.FireBase

data class ChoiceDto(
    val id: String = "",
    val text: String = "",
    val isCorrect: Boolean = false
)

data class QuestionDto(
    val id: String = "",
    val text: String = "",
    val type: String = "", // save as enum name e.g. "MULTIPLE_CHOICE_SINGLE"
    val points: Int = 0,
    val choices: List<ChoiceDto> = emptyList(),
    val essayAnswer: String? = null
)

data class ExamDto(
    val className: String = "",
    val examTitle: String = "",
    val examType: String = "",
    val examDate: String = "",
    val examStartTime: String = "",
    val examTime: String = "",
    val randomQuestions: Boolean = false,
    val showResultsImmediately: Boolean = false,
    val teacherId: String = "",
    val teacherName: String = "",
    val createdAt: Any? = null, // use serverTimestamp() or ISO string
    val questions: List<QuestionDto> = emptyList()
)
