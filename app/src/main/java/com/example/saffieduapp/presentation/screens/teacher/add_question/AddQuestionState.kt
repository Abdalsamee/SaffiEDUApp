package com.example.saffieduapp.presentation.screens.teacher.add_question

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// ١. تعريف أنواع الأسئلة الممكنة
enum class QuestionType(val displayName: String) {
    MULTIPLE_CHOICE_SINGLE("اختيار من متعدد (إجابة واحدة)"),
    MULTIPLE_CHOICE_MULTIPLE("اختيار من متعدد (عدة إجابات)"),
    TRUE_FALSE("صح وخطأ"),
    ESSAY("سؤال مقالي")
}

// ٢. تعريف الخيار الواحد في سؤال الاختيار من متعدد
data class Choice(
    val id: Long = System.currentTimeMillis(), // ID فريد لكل خيار
    var text: String = "",
    var isCorrect: Boolean = false
)

// ٣. تعريف الحالة الكاملة لشاشة إضافة السؤال
data class AddQuestionState(
    // معلومات السؤال الحالية التي يتم تحريرها
    val currentQuestionText: String = "",
    val currentQuestionType: QuestionType = QuestionType.MULTIPLE_CHOICE_SINGLE,
    val currentQuestionPoints: String = "1",

    // قائمة الخيارات للسؤال الحالي (من نوع خاص يمكن لـ Compose مراقبته)
    val currentChoices: SnapshotStateList<Choice> = mutableStateListOf(Choice(text = ""), Choice(text = "")),

    // الإجابة للسؤال المقالي
    val currentEssayAnswer: String = "",

    // قائمة بكل الأسئلة التي تم إنشاؤها في هذه الجلسة
    val createdQuestions: List<Any> = emptyList(), // سنقوم بتطويرها لاحقًا

    val isSaving: Boolean = false
)