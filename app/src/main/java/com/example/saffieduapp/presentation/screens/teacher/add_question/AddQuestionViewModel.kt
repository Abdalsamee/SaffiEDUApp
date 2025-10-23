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
                QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> listOf(
                    Choice(), Choice()
                )

                QuestionType.ESSAY -> emptyList()
            }
            currentState.copy(
                currentQuestionType = type, currentChoices = newChoices.toMutableStateList()
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
            val isEditMode = currentState.isEditing

            if (!validateQuestion(currentState)) {
                return@launch
            }

            // تحويل الحالة الحالية إلى QuestionData
            val questionData = QuestionData(
                id = currentState.questionBeingEditedId ?: java.util.UUID.randomUUID()
                    .toString(), // **استخدم المعرف القديم في وضع التعديل**
                text = currentState.currentQuestionText,
                type = currentState.currentQuestionType,
                points = currentState.currentQuestionPoints,
                choices = currentState.currentChoices.toList(), // نسخ القائمة
                essayAnswer = currentState.currentEssayAnswer
            )

            // 3. تحديث قائمة createdQuestions بناءً على وضع الإضافة أو التعديل
            val newQuestionsList = if (isEditMode) {
                // وضع التعديل: استبدل السؤال القديم بالسؤال الجديد (المحدث)
                currentState.createdQuestions.map { q ->
                    if (q.id == questionData.id) questionData else q
                }
            } else {
                // وضع الإضافة: أضف السؤال الجديد إلى القائمة
                currentState.createdQuestions + questionData
            }
            // تحديث الحالة مع إضافة السؤال إلى القائمة وإعادة تهيئة الحقول
            _state.update {
                it.copy(
                    currentQuestionText = "",
                    currentQuestionPoints = "",
                    currentChoices = when (currentState.currentQuestionType) {
                        QuestionType.TRUE_FALSE -> mutableStateListOf(
                            Choice(text = "صح"), Choice(text = "خطأ")
                        )

                        QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> mutableStateListOf(
                            Choice(), Choice()
                        )

                        QuestionType.ESSAY -> mutableStateListOf()
                    },
                    currentEssayAnswer = "",
                    createdQuestions = newQuestionsList, // <--- استخدم القائمة المحدثة
                    isEditing = false, // <--- مهم: الخروج من وضع التعديل
                    questionBeingEditedId = null
                )
            }
        }
    }

    fun getCreatedQuestions(): List<QuestionData> {
        return _state.value.createdQuestions
    }

    fun saveCurrentQuestionAndResetSync(): QuestionData? {
        val currentState = state.value

        if (!validateQuestion(currentState)) {
            return null // إرجاع قيمة فارغة إذا كان التحقق خاطئًا
        }
        val questionData = QuestionData(
            id = currentState.questionBeingEditedId ?: java.util.UUID.randomUUID().toString(),
            text = currentState.currentQuestionText,
            type = currentState.currentQuestionType,
            points = currentState.currentQuestionPoints,
            choices = currentState.currentChoices.toList(),
            essayAnswer = currentState.currentEssayAnswer
        )

        _state.update {
            it.copy(
                currentQuestionText = "",
                currentQuestionPoints = "",
                currentChoices = when (currentState.currentQuestionType) {
                    QuestionType.TRUE_FALSE -> mutableStateListOf(
                        Choice(text = "صح"), Choice(text = "خطأ")
                    )

                    QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> mutableStateListOf(
                        Choice(), Choice()
                    )

                    QuestionType.ESSAY -> mutableStateListOf()
                },
                currentEssayAnswer = "",
                createdQuestions = it.createdQuestions + questionData,
                isEditing = false, // <--- مهم: الخروج من وضع التعديل
                questionBeingEditedId = null
            )
        }
        return questionData
    }

    // دالة تهيئة لعملية التعديل
    fun setQuestionForEditing(questionData: QuestionData) {
        _state.update {
            it.copy(
                isEditing = true,
                questionBeingEditedId = questionData.id,
                currentQuestionText = questionData.text,
                currentQuestionType = questionData.type,
                currentQuestionPoints = questionData.points,
                currentChoices = questionData.choices.toMutableStateList(),
                currentEssayAnswer = questionData.essayAnswer
            )
        }
    }

    // **دالة مساعدة للتحقق من صحة المدخلات**
    private fun validateQuestion(currentState: AddQuestionState): Boolean {
        if (currentState.currentQuestionText.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(AddQuestionUiEvent.ShowToast("نص السؤال لا يمكن أن يكون فارغًا."))
            }
            return false
        }
        if (currentState.currentQuestionPoints.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(AddQuestionUiEvent.ShowToast("يجب تحديد عدد النقاط للسؤال."))
            }
            return false
        }

        when (currentState.currentQuestionType) {
            QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                // يجب أن تحتوي الخيارات على نص وتحديد إجابة صحيحة واحدة على الأقل
                val hasEmptyChoice = currentState.currentChoices.any { it.text.isBlank() }
                if (hasEmptyChoice) {
                    viewModelScope.launch {
                        _eventFlow.emit(AddQuestionUiEvent.ShowToast("لا يمكن ترك خيار إجابة فارغًا."))
                    }
                    return false
                }
                val hasCorrectChoice = currentState.currentChoices.any { it.isCorrect }
                if (!hasCorrectChoice) {
                    viewModelScope.launch {
                        _eventFlow.emit(AddQuestionUiEvent.ShowToast("يجب تحديد إجابة صحيحة واحدة على الأقل."))
                    }
                    return false
                }
            }

            QuestionType.TRUE_FALSE -> {
                // في "صح وخطأ"، يجب تحديد واحدة على أنها الإجابة الصحيحة
                val hasCorrectChoice = currentState.currentChoices.any { it.isCorrect }
                if (!hasCorrectChoice) {
                    viewModelScope.launch {
                        _eventFlow.emit(AddQuestionUiEvent.ShowToast("يجب تحديد 'صح' أو 'خطأ' كإجابة صحيحة."))
                    }
                    return false
                }
            }

            QuestionType.ESSAY -> {
                // يجب أن تكون الإجابة المقالية غير فارغة (للمقارنة لاحقًا)
                if (currentState.currentEssayAnswer.isBlank()) {
                    viewModelScope.launch {
                        _eventFlow.emit(AddQuestionUiEvent.ShowToast("يجب كتابة الإجابة المقالية النموذجية."))
                    }
                    return false
                }
            }
        }
        return true
    }
}