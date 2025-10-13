package com.example.saffieduapp.presentation.screens.student.exam_screen

import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType

/**
 * حالة شاشة الاختبار الكاملة
 */
data class ExamState(
    val examId: String = "",
    val examTitle: String = "",
    val examDate: String = "",
    val examStartTime: String = "",
    val totalQuestions: Int = 0,
    val currentQuestionIndex: Int = 0,
    val questions: List<ExamQuestion> = emptyList(),
    val userAnswers: Map<String, ExamAnswer> = emptyMap(), // questionId -> Answer
    val remainingTimeInSeconds: Int = 0,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val showTimeWarning: Boolean = false // عرض تحذير الوقت (آخر دقيقة)
)

/**
 * نموذج السؤال في الاختبار
 */
data class ExamQuestion(
    val id: String,
    val text: String,
    val type: QuestionType,
    val points: Int,
    val choices: List<Choice> = emptyList(), // فارغة للأسئلة المقالية
    val essayText: String = "" // 🔴 أضف هذا للحقول المقالية
)

/**
 * أنواع الإجابات المختلفة
 */
sealed class ExamAnswer {
    /**
     * إجابة اختيار من متعدد (إجابة واحدة)
     */
    data class SingleChoice(val choiceId: String) : ExamAnswer()

    /**
     * إجابة اختيار من متعدد (عدة إجابات)
     */
    data class MultipleChoice(val choiceIds: List<String>) : ExamAnswer()

    /**
     * إجابة صح/خطأ (تعتبر نوع من SingleChoice)
     */
    data class TrueFalse(val choiceId: String) : ExamAnswer()

    /**
     * إجابة سؤال مقالي
     */
    data class Essay(val text: String) : ExamAnswer()
}