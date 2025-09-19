package com.example.saffieduapp.presentation.screens.teacher.add_exam
// File: app/src/main/java/com/example/saffieduapp/data/model/TestData.kt

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// تعريف أنواع الاختبارات (لم يتغير)
enum class TestType(val displayName: String) {
    SHORT_TEST("اختبار قصير"),
    MID_TERM("اختبار نصفي"),
    FINAL_EXAM("اختبار نهائي")
}

// نموذج بيانات الاختبار
data class TestData(
    val id: String? = null,
    val className: String = "", // الصف الدراسي
    val title: String = "", // عنوان الاختبار
    val type: TestType = TestType.SHORT_TEST, // نوع الاختبار
    val dateString: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time), // <<<< تحديث هنا: تاريخ الاختبار كسلسلة
    val durationMinutes: Int = 0, // مدة الاختبار بالدقائق
    val shuffleQuestions: Boolean = false, // ترتيب الأسئلة عشوائيا
    val showResultsImmediately: Boolean = false, // عرض النتائج مباشرة بعد الانتهاء
    val questions: List<String> = emptyList() // قائمة الأسئلة (يمكن توسيعها لاحقاً)
)