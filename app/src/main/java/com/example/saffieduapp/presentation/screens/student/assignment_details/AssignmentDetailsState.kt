package com.example.saffieduapp.presentation.screens.student.assignment_details

// ١. تعريف موديل تفاصيل الواجب
data class AssignmentDetails(
    val id: String,
    val title: String,
    val description: String?, // قد لا يكون هناك وصف
    val imageUrl: String?, // قد لا تكون هناك صورة
    val subjectName: String,
    val teacherName: String,
    val dueDate: String,
    val remainingTime: String
)

// ٢. تعريف الحالة الكاملة للشاشة
data class AssignmentDetailsState(
    val isLoading: Boolean = true,
    val assignmentDetails: AssignmentDetails? = null
)