package com.example.saffieduapp.presentation.screens.teacher.add_question

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.UUID

enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE_SINGLE("اختيار من متعدد (إجابة واحدة)"),
    MULTIPLE_CHOICE_MULTIPLE("اختيار من متعدد (عدة إجابات)"),
    TRUE_FALSE("صح وخطأ"),
    ESSAY("سؤال مقالي")
}

data class Choice(
    val id: String = UUID.randomUUID().toString(), // Using UUID is more robust
    var text: String = "",
    var isCorrect: Boolean = false
)

data class AddQuestionState(
    val currentQuestionText: String = "",
    val currentQuestionType: QuestionType = QuestionType.MULTIPLE_CHOICE_SINGLE,
    val currentQuestionPoints: String = "",
    val currentChoices: SnapshotStateList<Choice> = mutableStateListOf(Choice(), Choice()),
    val currentEssayAnswer: String = "",
    val createdQuestions: List<Any> = emptyList(),
    val isSaving: Boolean = false
)
