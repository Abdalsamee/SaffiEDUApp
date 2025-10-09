package com.example.saffieduapp.presentation.screens.teacher.tasks


enum class TeacherTasksTab {
    HOMEWORKS,
    EXAMS
}

data class TeacherTasksState(
    val selectedTabIndex: Int = 0 // 0 = الواجبات, 1 = الاختبارات
)
