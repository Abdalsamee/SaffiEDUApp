package com.example.saffieduapp.presentation.screens.teacher.add_exam

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddExamViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AddExamState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddExamEvent) {
        when (event) {
            is AddExamEvent.ClassSelected -> _state.update { it.copy(selectedClass = event.className) }
            is AddExamEvent.TitleChanged -> _state.update { it.copy(examTitle = event.title) }
            is AddExamEvent.TypeChanged -> _state.update { it.copy(examType = event.type) }
            is AddExamEvent.DateChanged -> _state.update { it.copy(examDate = event.date) }
            is AddExamEvent.TimeChanged -> _state.update { it.copy(examTime = event.time) }
            is AddExamEvent.RandomQuestionsToggled -> _state.update { it.copy(randomQuestions = event.isEnabled) }
            is AddExamEvent.ShowResultsToggled -> _state.update { it.copy(showResultsImmediately = event.isEnabled) }
            is AddExamEvent.NextClicked -> {
                // TODO: Validate data and navigate to the next screen (Add Questions)
                println("Navigating to next step with state: ${state.value}")
            }
        }
    }
}