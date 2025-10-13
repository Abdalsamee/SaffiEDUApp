package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

            try {
                if (examId.isBlank()) {
                    Log.e("ExamViewModel", "loadExamData: examId فارغ")
                    _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: معرف الاختبار فارغ"))
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                val examDoc = FirebaseFirestore.getInstance()
                    .collection("exams")
                    .document(examId)
                    .get()
                    .await()

                val examData = examDoc.data
                if (examData == null) {
                    Log.e("ExamViewModel", "loadExamData: لم يتم العثور على المستند examId=$examId")
                    _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: لم يتم العثور على بيانات الاختبار"))
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                // examTitle
                val examTitle = examData["examTitle"] as? String ?: examData["title"] as? String ?: "اختبار"

                // استخراج الأسئلة بطريقة مرنة
                val rawQuestions = examData["questions"] as? List<*> ?: emptyList<Any>()
                val questions = rawQuestions.mapNotNull { item ->
                    (item as? Map<*, *>)?.let { q ->
                        try {
                            val id = (q["id"] ?: q["questionId"] ?: q["uid"])?.toString() ?: return@let null
                            val text = (q["text"] ?: q["questionText"])?.toString() ?: ""
                            val typeStr = (q["type"] ?: "ESSAY").toString()
                            val type = try {
                                QuestionType.valueOf(typeStr)
                            } catch (ex: Exception) {
                                Log.w("ExamViewModel", "غير معروف QuestionType: $typeStr, افتراض ESSAY")
                                QuestionType.ESSAY
                            }
                            val points = when (val p = q["points"]) {
                                is Number -> p.toInt()
                                is String -> p.toIntOrNull() ?: 0
                                else -> 0
                            }

                            val rawChoices = q["choices"] as? List<*> ?: emptyList<Any>()
                            val choices = rawChoices.mapNotNull { rc ->
                                (rc as? Map<*, *>)?.let { c ->
                                    val cid = (c["id"] ?: c["choiceId"])?.toString() ?: return@let null
                                    val ctext = (c["text"] ?: c["choiceText"])?.toString() ?: ""
                                    val isCorrect = when (val ic = c["isCorrect"]) {
                                        is Boolean -> ic
                                        is String -> ic.equals("true", ignoreCase = true)
                                        is Number -> ic.toInt() != 0
                                        else -> false
                                    }
                                    Choice(id = cid, text = ctext, isCorrect = isCorrect)
                                }
                            }

                            ExamQuestion(
                                id = id,
                                text = text,
                                type = type,
                                points = points,
                                choices = choices
                            )
                        } catch (ex: Exception) {
                            Log.e("ExamViewModel", "خطأ عند تحويل سؤال: ${ex.message}")
                            null
                        }
                    }
                }

                // examTime قد يكون Long أو String
                val examTimeMinutes = when (val t = examData["examTime"]) {
                    is Number -> t.toInt()
                    is String -> t.toIntOrNull() ?: 0
                    else -> 0
                }
                val remainingTimeInSeconds = examTimeMinutes * 60

                _state.update {
                    it.copy(
                        examId = examId,
                        examTitle = examTitle,
                        totalQuestions = questions.size,
                        questions = questions,
                        remainingTimeInSeconds = remainingTimeInSeconds,
                        isLoading = false
                    )
                }

                // إذا وجدنا أسئلة فارغة نعرض تنبيه للمطور
                if (questions.isEmpty()) {
                    Log.w("ExamViewModel", "لا توجد أسئلة بعد التحويل - تحقق من هيكل البيانات في Firestore (examId=$examId)")
                    _eventFlow.emit(ExamUiEvent.ShowToast("لا توجد أسئلة في الاختبار"))
                }

                // بدء المؤقت
                startTimer()

            } catch (e: Exception) {
                Log.e("ExamViewModel", "loadExamData error", e)
                _eventFlow.emit(ExamUiEvent.ShowToast("فشل تحميل الاختبار: ${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
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
            is ExamEvent.ToggleMultipleChoice -> toggleMultipleChoice(
                event.questionId,
                event.choiceId
            )

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
            timerJob?.cancel()

            val email = FirebaseAuth.getInstance().currentUser?.email
            if (email.isNullOrEmpty()) {
                _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: لم يتم تسجيل الدخول"))
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            try {

                val userDoc = FirebaseFirestore.getInstance()
                    .collection("students")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                val studentId = userDoc?.id
                if (studentId.isNullOrEmpty()) {
                    _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: لم يتم العثور على بيانات الطالب"))
                    _state.update { it.copy(isSubmitting = false) }
                    return@launch
                }

                val answersMap = _state.value.userAnswers.mapValues { entry ->
                    when (val ans = entry.value) {
                        is ExamAnswer.SingleChoice -> ans.choiceId
                        is ExamAnswer.MultipleChoice -> ans.choiceIds
                        is ExamAnswer.TrueFalse -> ans.choiceId
                        is ExamAnswer.Essay -> ans.text
                    }
                }

                // حفظ الإجابات باستخدام studentId الذي تم الحصول عليه من البريد
                FirebaseFirestore.getInstance()
                    .collection("exam_submissions")
                    .document(_state.value.examId)
                    .collection("submissions")
                    .document(studentId)
                    .set(
                        mapOf(
                            "answers" to answersMap,
                            "submittedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

                _state.update { it.copy(isSubmitting = false) }
                _eventFlow.emit(ExamUiEvent.ExamCompleted)

            } catch (e: Exception) {
                _state.update { it.copy(isSubmitting = false) }
                _eventFlow.emit(ExamUiEvent.ShowToast("فشل تسليم الاختبار: ${e.message}"))
            }
        }
    }

    fun saveAnswersTemporarily() {
        viewModelScope.launch {
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return@launch
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("students")
                    .whereEqualTo("email", email)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                val studentId = userDoc?.id ?: return@launch

                val answersMap = _state.value.userAnswers.mapValues { entry ->
                    when (val ans = entry.value) {
                        is ExamAnswer.SingleChoice -> ans.choiceId
                        is ExamAnswer.MultipleChoice -> ans.choiceIds
                        is ExamAnswer.TrueFalse -> ans.choiceId
                        is ExamAnswer.Essay -> ans.text
                    }
                }

                FirebaseFirestore.getInstance()
                    .collection("exam_submissions")
                    .document(_state.value.examId)
                    .collection("drafts") // تخزين مؤقت
                    .document(studentId)
                    .set(
                        mapOf(
                            "answers" to answersMap,
                            "savedAt" to System.currentTimeMillis()
                        )
                    ).await()
            } catch (_: Exception) {
                // يمكن تجاهل الأخطاء هنا أو تسجيلها
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}