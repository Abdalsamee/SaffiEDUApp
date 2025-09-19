package com.example.saffieduapp.presentation.screens.teacher.add_question

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddQuestionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AddQuestionState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddQuestionEvent) {
        when (event) {
            is AddQuestionEvent.QuestionTypeChanged -> handleQuestionTypeChange(event.type)
            is AddQuestionEvent.QuestionTextChanged -> _state.update { it.copy(currentQuestionText = event.text) }
            is AddQuestionEvent.PointsChanged -> _state.update { it.copy(currentQuestionPoints = event.points) }
            is AddQuestionEvent.AddChoiceClicked -> addChoice()
            is AddQuestionEvent.RemoveChoiceClicked -> removeChoice(event.choiceId)
            is AddQuestionEvent.ChoiceTextChanged -> updateChoiceText(event.choiceId, event.text)
            is AddQuestionEvent.CorrectChoiceSelected -> selectCorrectChoice(event.choiceId)
            is AddQuestionEvent.AddNewQuestionClicked -> saveCurrentQuestionAndReset()
        }
    }

    private fun handleQuestionTypeChange(type: QuestionType) {
        _state.update { currentState ->
            val newChoices = when (type) {
                QuestionType.TRUE_FALSE -> mutableStateListOf(Choice(text = "صح", isCorrect = true), Choice(text = "خطأ"))
                QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> mutableStateListOf(Choice(), Choice())
                QuestionType.ESSAY -> mutableStateListOf()
            }
            currentState.copy(
                currentQuestionType = type,
                currentChoices = newChoices
            )
        }
    }

    private fun addChoice() {
        if (state.value.currentChoices.size < 5) {
            _state.value.currentChoices.add(Choice())
        }
    }

    private fun removeChoice(choiceId: Long) {
        _state.value.currentChoices.removeIf { it.id == choiceId }
    }

    private fun updateChoiceText(choiceId: Long, text: String) {
        val choiceIndex = state.value.currentChoices.indexOfFirst { it.id == choiceId }
        if (choiceIndex != -1) {
            _state.value.currentChoices[choiceIndex] = _state.value.currentChoices[choiceIndex].copy(text = text)
        }
    }

    private fun selectCorrectChoice(choiceId: Long) {
        val currentType = state.value.currentQuestionType
        val updatedChoices = state.value.currentChoices.map { choice ->
            when (currentType) {
                QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.TRUE_FALSE -> {
                    // تحديد خيار واحد فقط
                    choice.copy(isCorrect = choice.id == choiceId)
                }
                QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                    // عكس حالة الخيار المحدد
                    if (choice.id == choiceId) choice.copy(isCorrect = !choice.isCorrect) else choice
                }
                else -> choice
            }
        }.toMutableStateList()
        _state.update { it.copy(currentChoices = updatedChoices) }
    }

    private fun saveCurrentQuestionAndReset() {
        // TODO: Validate the current question before saving
        // TODO: Create a proper Question domain model and add the created question to the list
        println("Question Saved (temporarily): ${state.value}")

        // Reset for the next question
        _state.value = AddQuestionState()
    }
}