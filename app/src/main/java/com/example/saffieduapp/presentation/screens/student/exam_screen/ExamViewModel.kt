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

@Suppress("DUPLICATE_BRANCH_CONDITION_IN_WHEN")
@HiltViewModel
class ExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ExamState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ExamUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null


    /**
     * تحميل بيانات الاختبار من Firebase Firestore مع التصحيح لنوع الأسئلة
     */
    fun loadExam(examId: String) {
        // احجز إذا تم استدعاؤها مرتين لنفس الـ id
        if (_state.value.examId == examId && !_state.value.isLoading && _state.value.questions.isNotEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                if (examId.isBlank()) {
                    Log.e("ExamViewModel", "loadExam: examId فارغ")
                    _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: معرف الاختبار فارغ"))
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                val examDoc = firestore.collection("exams").document(examId).get().await()
                if (!examDoc.exists()) {
                    _eventFlow.emit(ExamUiEvent.ShowToast("خطأ: لم يتم العثور على بيانات الاختبار"))
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }

                val examData = examDoc.data ?: emptyMap<String, Any>()
                val examTitle = examData["examTitle"] as? String ?: "اختبار بدون عنوان"
                val examDate = examData["examDate"] as? String ?: ""
                val examStartTime = examData["examStartTime"] as? String ?: ""
                val examTimeMinutes = when (val t = examData["examTime"]) {
                    is Number -> t.toInt()
                    is String -> t.toIntOrNull() ?: 60
                    else -> 60
                }

                // تحويل الأسئلة (نفس منطقك السابق)
                val rawQuestions = examData["questions"] as? List<*> ?: emptyList<Any>()

                val questions = rawQuestions.mapIndexed { index, item ->
                    val questionMap = item as? Map<*, *> ?: emptyMap<Any, Any>()
                    val id = questionMap["id"]?.toString() ?: "q_$index"
                    val text = questionMap["text"]?.toString() ?: "سؤال بدون نص"
                    val typeStr = (questionMap["type"] as? String) ?: "ESSAY"
                    val type = convertToQuestionType(typeStr)
                    val points = when (val p = questionMap["points"]) {
                        is Number -> p.toInt()
                        is String -> p.toIntOrNull() ?: 1
                        else -> 1
                    }

                    val choices = mutableListOf<Choice>()
                    val rawChoices = questionMap["choices"] as? List<*> ?: emptyList<Any>()
                    rawChoices.forEachIndexed { cIndex, cItem ->
                        val cMap = cItem as? Map<*, *> ?: emptyMap<Any, Any>()
                        val cid = cMap["id"]?.toString() ?: "c_${index}_$cIndex"
                        val ctext = cMap["text"]?.toString() ?: ""
                        val isCorrect = when (val ic = cMap["isCorrect"]) {
                            is Boolean -> ic
                            is String -> ic.equals("true", ignoreCase = true)
                            is Number -> ic.toInt() != 0
                            else -> false
                        }
                        choices.add(Choice(id = cid, text = ctext, isCorrect = isCorrect))
                    }

                    if (type == QuestionType.TRUE_FALSE && choices.isEmpty()) {
                        choices.add(Choice(id = "true_$index", text = "صح", isCorrect = true))
                        choices.add(Choice(id = "false_$index", text = "خطأ", isCorrect = false))
                    }

                    val essayAnswerRaw = questionMap["essayAnswer"]
                    val essayText = when (essayAnswerRaw) {
                        is String -> essayAnswerRaw
                        is Map<*, *> -> (essayAnswerRaw["text"] as? String) ?: ""
                        else -> ""
                    }

                    ExamQuestion(
                        id = id,
                        text = text,
                        type = type,
                        points = points,
                        choices = choices,
                        essayText = essayText
                    )
                }

                _state.update {
                    it.copy(
                        examId = examId,
                        examTitle = examTitle,
                        examDate = examDate,
                        examStartTime = examStartTime,
                        totalQuestions = questions.size,
                        questions = questions,
                        remainingTimeInSeconds = examTimeMinutes * 60,
                        isLoading = false
                    )
                }

                if (questions.isEmpty()) {
                    _eventFlow.emit(ExamUiEvent.ShowToast("لا توجد أسئلة في الاختبار"))
                } else {
                    startTimer()
                }

            } catch (e: Exception) {
                Log.e("ExamViewModel", "خطأ في تحميل بيانات الاختبار", e)
                _eventFlow.emit(ExamUiEvent.ShowToast("فشل تحميل الاختبار: ${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * 🔴 دالة مساعدة لتحويل نوع السؤال من String إلى QuestionType
     */
    private fun convertToQuestionType(typeStr: String): QuestionType {
        return when (typeStr.uppercase().trim()) {
            "MULTIPLE_CHOICE_SINGLE", "MULTIPLE_CHOICE" -> QuestionType.MULTIPLE_CHOICE_SINGLE
            "MULTIPLE_CHOICE_MULTIPLE" -> QuestionType.MULTIPLE_CHOICE_MULTIPLE
            "TRUE_FALSE" -> QuestionType.TRUE_FALSE
            "ESSAY" -> QuestionType.ESSAY
            else -> {
                Log.w("ExamViewModel", "نوع سؤال غير معروف: $typeStr, استخدام ESSAY كافتراضي")
                QuestionType.ESSAY
            }
        }
    }

    //start timer time end exam
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.remainingTimeInSeconds > 0) {
                delay(1000)
                onEvent(ExamEvent.TickTimer)
            }
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
            //is ExamEvent.SelectSingleChoice -> updateTrueFalseAnswer(event.questionId, event.choiceId)
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
        saveAnswersTemporarily()
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
        saveAnswersTemporarily()
    }

    private fun updateEssayAnswer(questionId: String, text: String) {
        val newAnswers = _state.value.userAnswers.toMutableMap()
        newAnswers[questionId] = ExamAnswer.Essay(text)
        _state.update { it.copy(userAnswers = newAnswers) }
        saveAnswersTemporarily()
    }

    private fun updateTrueFalseAnswer(questionId: String, choiceId: String) {
        val newAnswers = _state.value.userAnswers.toMutableMap()
        newAnswers[questionId] = ExamAnswer.TrueFalse(choiceId)
        _state.update { it.copy(userAnswers = newAnswers) }
        saveAnswersTemporarily()
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
                val userDoc = firestore
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

                val submissionDocId = "${_state.value.examId}_$studentId"

                // حفظ الإجابات النهائية
                firestore
                    .collection("exam_submissions")
                    .document(submissionDocId) // استخدام تركيبة examId_studentId كـ ID للتسليم
                    .set(
                        mapOf(
                            "answers" to answersMap,
                            "submittedAt" to System.currentTimeMillis(),
                            "studentId" to studentId,
                            "examId" to _state.value.examId
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
            val email = auth.currentUser?.email ?: return@launch
            try {
                val userDoc = firestore
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

                val draftDocId = "${_state.value.examId}_${studentId}_draft"

                FirebaseFirestore.getInstance()
                    .collection("draft")
                    .document(draftDocId)
                    .set(
                        mapOf(
                            "answers" to answersMap,
                            "savedAt" to System.currentTimeMillis(),
                            "studentId" to studentId,
                            "examId" to _state.value.examId, // إضافة examId لتسهيل الاستعلام
                            "isDraft" to true // حقل إضافي للتمييز
                        )
                    ).await()
            } catch (_: Exception) {
                // تجاهل الأخطاء في الحفظ المؤقت
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        saveAnswersTemporarily()
    }
}