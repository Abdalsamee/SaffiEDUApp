package com.example.saffieduapp.presentation.screens.teacher.add_question

sealed interface AddQuestionEvent {
    data class QuestionTypeChanged(val type: QuestionType) : AddQuestionEvent
    data class QuestionTextChanged(val text: String) : AddQuestionEvent
    data class PointsChanged(val points: String) : AddQuestionEvent
    data class ChoiceTextChanged(val choiceId: Long, val text: String) : AddQuestionEvent
    data class CorrectChoiceSelected(val choiceId: Long) : AddQuestionEvent
    data object AddChoiceClicked : AddQuestionEvent
    data class RemoveChoiceClicked(val choiceId: Long) : AddQuestionEvent
    data object AddNewQuestionClicked : AddQuestionEvent
}