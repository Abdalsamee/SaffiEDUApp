package com.example.saffieduapp.presentation.screens.teacher.add_question

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddQuestionUiEvent {
    data class ShowToast(val message: String) : AddQuestionUiEvent()
}

@HiltViewModel
class AddQuestionViewModel @Inject constructor() : ViewModel() {
    private val _eventFlow = MutableSharedFlow<AddQuestionUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
            is AddQuestionEvent.EssayAnswerChanged -> {
                _state.update { it.copy(currentEssayAnswer = event.answer) }
            }
        }
    }

    private fun handleQuestionTypeChange(type: QuestionType) {
        _state.update { currentState ->
            val newChoices = when (type) {
                QuestionType.TRUE_FALSE -> listOf(Choice(text = "صح"), Choice(text = "خطأ"))
                QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> listOf(Choice(), Choice())
                QuestionType.ESSAY -> emptyList()
            }
            currentState.copy(
                currentQuestionType = type,
                currentChoices = newChoices.toMutableStateList()
            )
        }
    }

    private fun addChoice() {
        if (_state.value.currentChoices.size < 5) {
            _state.update { it.copy(currentChoices = (it.currentChoices + Choice()).toMutableStateList()) }
        }
    }

    private fun removeChoice(choiceId: String) {
        if (_state.value.currentChoices.size > 2) {
            _state.update { currentState ->
                val newChoices = currentState.currentChoices.filter { it.id != choiceId }
                currentState.copy(currentChoices = newChoices.toMutableStateList())
            }
        }
    }

    private fun updateChoiceText(choiceId: String, newText: String) {
        _state.update { currentState ->
            val newChoices = currentState.currentChoices.map { choice ->
                if (choice.id == choiceId) choice.copy(text = newText) else choice
            }
            currentState.copy(currentChoices = newChoices.toMutableStateList())
        }
    }

    private fun selectCorrectChoice(choiceId: String) {
        _state.update { currentState ->
            val currentType = currentState.currentQuestionType
            val newChoices = currentState.currentChoices.map { choice ->
                when (currentType) {
                    QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.TRUE_FALSE -> {
                        choice.copy(isCorrect = choice.id == choiceId)
                    }
                    QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                        if (choice.id == choiceId) choice.copy(isCorrect = !choice.isCorrect) else choice
                    }
                    else -> choice
                }
            }
            currentState.copy(currentChoices = newChoices.toMutableStateList())
        }
    }

    private fun saveCurrentQuestionAndReset() {
        viewModelScope.launch {
            val currentState = state.value

            // تحويل الحالة الحالية إلى QuestionData
            val questionData = QuestionData(
                text = currentState.currentQuestionText,
                type = currentState.currentQuestionType,
                points = currentState.currentQuestionPoints,
                choices = currentState.currentChoices.toList(), // نسخ القائمة
                essayAnswer = currentState.currentEssayAnswer
            )

            // تحديث الحالة مع إضافة السؤال إلى القائمة وإعادة تهيئة الحقول
            _state.update {
                it.copy(
                    currentQuestionText = "",
                    currentQuestionPoints = "",
                    currentChoices = when (currentState.currentQuestionType) {
                        QuestionType.TRUE_FALSE -> mutableStateListOf(Choice(text = "صح"), Choice(text = "خطأ"))
                        QuestionType.MULTIPLE_CHOICE_SINGLE,
                        QuestionType.MULTIPLE_CHOICE_MULTIPLE -> mutableStateListOf(Choice(), Choice())
                        QuestionType.ESSAY -> mutableStateListOf()
                    },
                    currentEssayAnswer = "",
                    createdQuestions = it.createdQuestions + questionData
                )
            }
        }
    }
    fun getCreatedQuestions(): List<QuestionData> {
        return _state.value.createdQuestions
    }

}