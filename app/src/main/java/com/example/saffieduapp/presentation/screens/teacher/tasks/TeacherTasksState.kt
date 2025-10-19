package com.example.saffieduapp.presentation.screens.teacher.tasks

import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType

data class TeacherTasksState(
    val selectedTabIndex: Int = 0,
    val selectedClass: String = "الصف السادس",
    val isLoading: Boolean = false,
    val assignments: List<TeacherTaskItem> = emptyList(),
    val exams: List<TeacherTaskItem> = emptyList()
)

data class TeacherTaskItem(
    val id: String = "",
    val subject: String,
    val date: String,
    val time: String,
    val isActive: Boolean,
    val type: TaskType,
    val title: String?
)
