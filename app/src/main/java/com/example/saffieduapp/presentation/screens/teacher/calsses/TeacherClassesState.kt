package com.example.saffieduapp.presentation.screens.teacher.calsses


// ١. تعريف البيانات الخاصة ببطاقة الصف الواحد
data class ClassItem(
    val classId: String,
    val className: String,
    val subjectName: String,
    val subjectImageUrl: String,
    val quizCount: Int,
    val assignmentCount: Int,
    val videoLessonCount: Int,
    val pdfLessonCount: Int,
    val studentCount: Int
)

// ٢. تعريف الحالة الكاملة للشاشة
data class TeacherClassesState(
    val isLoading: Boolean = true,
    val classes: List<ClassItem> = emptyList()
)