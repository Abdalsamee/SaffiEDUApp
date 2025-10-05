package com.example.saffieduapp.presentation.screens.teacher.add_exam

sealed interface AddExamEvent {
    data class ClassSelected(val className: String) : AddExamEvent
    data class TitleChanged(val title: String) : AddExamEvent
    data class TypeChanged(val type: String) : AddExamEvent
    data class DateChanged(val date: String) : AddExamEvent
    data class StartTimeChanged(val time: String) : AddExamEvent
    data class TimeChanged(val time: String) : AddExamEvent
    data class RandomQuestionsToggled(val isEnabled: Boolean) : AddExamEvent
    data class ShowResultsToggled(val isEnabled: Boolean) : AddExamEvent
    data object NextClicked : AddExamEvent
    object SaveDraftClicked : AddExamEvent
}