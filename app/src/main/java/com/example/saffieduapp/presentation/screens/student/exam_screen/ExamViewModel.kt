package com.example.saffieduapp.presentation.screens.student.exam_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExamUiEvent {
    data class ShowToast(val message: String) : ExamUiEvent()
    data object ExamCompleted : ExamUiEvent()
    data object TimeExpired : ExamUiEvent()
}

@HiltViewModel
class ExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String = savedStateHandle.get<String>("examId") ?: ""

    private val _state = MutableStateFlow(ExamState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ExamUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        loadExamData()
    }

    /**
     * TODO: تحميل بيانات الاختبار من Firebase
     * هذه بيانات تجريبية ووهمية - سيتم استبدالها بمطور Firebase
     */
    private fun loadExamData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // TODO: استدعاء Firebase هنا
            delay(1000) // محاكاة التحميل

            // بيانات تجريبية
            val mockQuestions = listOf(
                ExamQuestion(
                    id = "q1",
                    text = "هو طلب للمعلومة أو المعرفة أو البيانات، وهو أسلوب يستخدم لجمع المعلومات، أو طلب شيء، أو طلب تركيع أو طلب شيء؟",
                    type = QuestionType.MULTIPLE_CHOICE_SINGLE,
                    points = 1,
                    choices = listOf(
                        Choice(id = "c1", text = "الخيار الأول", isCorrect = true),
                        Choice(id = "c2", text = "الخيار الثاني", isCorrect = false),
                        Choice(id = "c3", text = "الخيار الثالث", isCorrect = false),
                        Choice(id = "c4", text = "الخيار الرابع", isCorrect = false)
                    )
                ),
                ExamQuestion(
                    id = "q2",
                    text = "اختر الإجابات الصحيحة من التالي؟",
                    type = QuestionType.MULTIPLE_CHOICE_MULTIPLE,
                    points = 2,
                    choices = listOf(
                        Choice(id = "c1", text = "الخيار الأول", isCorrect = true),
                        Choice(id = "c2", text = "الخيار الثاني", isCorrect = true),
                        Choice(id = "c3", text = "الخيار الثالث", isCorrect = false),
                        Choice(id = "c4", text = "الخيار الرابع", isCorrect = false)
                    )
                ),
                ExamQuestion(
                    id = "q3",
                    text = "هل العبارة التالية صحيحة؟",
                    type = QuestionType.TRUE_FALSE,
                    points = 1,
                    choices = listOf(
                        Choice(id = "c1", text = "صح", isCorrect = true),
                        Choice(id = "c2", text = "خطأ", isCorrect = false)
                    )
                ),
                ExamQuestion(
                    id = "q4",
                    text = "اكتب موضوعاً عن أهمية التعليم؟",
                    type = QuestionType.ESSAY,
                    points = 5,
                    choices = emptyList()
                )
            )

            _state.update {
                it.copy(
                    examId = examId,
                    examTitle = "اختبار الوحدة الثانية",
                    totalQuestions = mockQuestions.size,
                    questions = mockQuestions,
                    remainingTimeInSeconds = 600, // 10 دقائق
                    isLoading = false
                )
            }

            // بدء المؤقت
            startTimer()
        }
    }

    /**
     * بدء العداد التنازلي
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.remainingTimeInSeconds > 0) {
                delay(1000)
                onEvent(ExamEvent.TickTimer)
            }
            // انتهى الوقت
            _eventFlow.emit(ExamUiEvent.TimeExpired)
            submitExam()
        }
    }

    fun onEvent(event: ExamEvent) {
        when (event) {
            is ExamEvent.SelectSingleChoice -> selectSingleChoice(event.questionId, event.choiceId)
            is ExamEvent.ToggleMultipleChoice -> toggleMultipleChoice(event.questionId, event.choiceId)
            is ExamEvent.UpdateEssayAnswer -> updateEssayAnswer(event.questionId, event.text)
            is ExamEvent.NextQuestion -> nextQuestion()
            is ExamEvent.PreviousQuestion -> previousQuestion()
            is ExamEvent.GoToQuestion -> goToQuestion(event.index)
            is ExamEvent.SubmitExam -> submitExam()
            is ExamEvent.TickTimer -> tickTimer()
        }
    }

    private fun selectSingleChoice(questionId: String, choiceId: String) {
        val newAnswers = _state.value.userAnswers.toMutableMap()
        newAnswers[questionId] = ExamAnswer.SingleChoice(choiceId)
        _state.update { it.copy(userAnswers = newAnswers) }
    }

    private fun toggleMultipleChoice(questionId: String, choiceId: String) {
        val currentAnswer = _state.value.userAnswers[questionId] as? ExamAnswer.MultipleChoice
        val currentIds = currentAnswer?.choiceIds ?: emptyList()

        val newIds = if (choiceId in currentIds) {
            currentIds - choiceId
        } else {
            currentIds + choiceId
        }

        val newAnswers = _state.value.userAnswers.toMutableMap()
        newAnswers[questionId] = ExamAnswer.MultipleChoice(newIds)
        _state.update { it.copy(userAnswers = newAnswers) }
    }

    private fun updateEssayAnswer(questionId: String, text: String) {
        val newAnswers = _state.value.userAnswers.toMutableMap()
        newAnswers[questionId] = ExamAnswer.Essay(text)
        _state.update { it.copy(userAnswers = newAnswers) }
    }

    private fun nextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val totalQuestions = _state.value.totalQuestions

        if (currentIndex < totalQuestions - 1) {
            _state.update { it.copy(currentQuestionIndex = currentIndex + 1) }
        }
    }

    private fun previousQuestion() {
        val currentIndex = _state.value.currentQuestionIndex

        if (currentIndex > 0) {
            _state.update { it.copy(currentQuestionIndex = currentIndex - 1) }
        }
    }

    private fun goToQuestion(index: Int) {
        if (index in 0 until _state.value.totalQuestions) {
            _state.update { it.copy(currentQuestionIndex = index) }
        }
    }

    private fun tickTimer() {
        val currentTime = _state.value.remainingTimeInSeconds
        if (currentTime > 0) {
            _state.update {
                it.copy(
                    remainingTimeInSeconds = currentTime - 1,
                    showTimeWarning = currentTime - 1 <= 60 // آخر دقيقة
                )
            }
        }
    }

    private fun submitExam() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            timerJob?.cancel() // إيقاف المؤقت

            // TODO: إرسال الإجابات إلى Firebase
            delay(1000) // محاكاة الإرسال

            _state.update { it.copy(isSubmitting = false) }
            _eventFlow.emit(ExamUiEvent.ExamCompleted)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}