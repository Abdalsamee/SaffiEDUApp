package com.example.saffieduapp.presentation.screens.student.tasks

// ١. تعريف موديل الاختبار الواحد
data class ExamItem(
    val id: String,
    val title: String,
    val subjectName: String,
    val imageUrl: String,
    val time: Long,
    val status: ExamStatus
)

// ٢. تعريف حالة الاختبار (مكتمل أم لا)
enum class ExamStatus {
    COMPLETED,
    NOT_COMPLETED,
    IN_PROGRESS
}

// ٣. تعريف موديل الواجب الواحد
data class AssignmentItem(
    val id: String,
    val title: String,
    val subjectName: String,
    val imageUrl: String,
    val dueDate: String, // "ينتهي في: ..."
    val remainingTime: String, // "متبقي 4 أيام"
    val status: AssignmentStatus
)

// ٤. تعريف حالة الواجب
enum class AssignmentStatus {
    SUBMITTED, // تم التسليم
    PENDING, // لم يسلم بعد
    LATE, // متأخر
    EXPIRED // انتهى (حالة نهائية)
}

// ٥. تعريف الحالة الكاملة للشاشة (اسم موحد)
data class TasksState(
    val isLoading: Boolean = true,
    val selectedTabIndex: Int = 0,
    val examsByDate: Map<String, List<ExamItem>> = emptyMap(),
    val assignmentsByDate: Map<String, List<AssignmentItem>> = emptyMap(),
    val error: String?
)