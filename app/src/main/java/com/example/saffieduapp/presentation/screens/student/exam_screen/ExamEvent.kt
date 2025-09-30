package com.example.saffieduapp.presentation.screens.student.exam_screen

/**
 * أحداث التفاعل مع شاشة الاختبار
 */
sealed interface ExamEvent {
    /**
     * اختيار إجابة واحدة (MCQ Single / True-False)
     */
    data class SelectSingleChoice(val questionId: String, val choiceId: String) : ExamEvent

    /**
     * اختيار/إلغاء اختيار في MCQ Multiple
     */
    data class ToggleMultipleChoice(val questionId: String, val choiceId: String) : ExamEvent

    /**
     * تغيير نص الإجابة المقالية
     */
    data class UpdateEssayAnswer(val questionId: String, val text: String) : ExamEvent

    /**
     * الانتقال للسؤال التالي
     */
    data object NextQuestion : ExamEvent

    /**
     * الانتقال للسؤال السابق
     */
    data object PreviousQuestion : ExamEvent

    /**
     * الانتقال لسؤال محدد بالترتيب
     */
    data class GoToQuestion(val index: Int) : ExamEvent

    /**
     * إنهاء الاختبار وتسليمه
     */
    data object SubmitExam : ExamEvent

    /**
     * تحديث العداد التنازلي (يتم استدعاؤه كل ثانية)
     */
    data object TickTimer : ExamEvent
}