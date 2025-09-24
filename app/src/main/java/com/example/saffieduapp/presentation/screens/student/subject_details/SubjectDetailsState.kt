package com.example.saffieduapp.presentation.screens.student.subject_details

// سنفترض أن هذه هي الـ Models الصحيحة، يمكنك تعديلها إذا لزم الأمر

import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfLesson
import java.util.Date

data class Alert(
    val id: String,
    val message: String,
    val dateTime: Date? = null
)
/**
 * يمثل التبويبات المتاحة في الشاشة.
 */
enum class SubjectTab {
    VIDEOS,
    PDFS
}

/**
 * يمثل الحالة الكاملة لواجهة تفاصيل المادة.
 */
data class SubjectDetailsState(
    // أبقيت على المتغيرات الأصلية وأضفت عليها
    val isLoading: Boolean = true,
    val subject: Subject? = null,
    val selectedTab: SubjectTab = SubjectTab.VIDEOS,
    val videoLessons: List<Lesson> = emptyList(),
    val pdfSummaries: List<PdfLesson> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",       // لتخزين نص البحث
    val alerts: List<Alert> = emptyList() // لتخزين قائمة التنبيهات
)