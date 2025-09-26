package com.example.saffieduapp.presentation.screens.student.tasks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state = _state.asStateFlow()

    init {
        loadTasks()
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTabIndex = index) }
    }

    private fun loadTasks() {
        val timeNow = Calendar.getInstance().timeInMillis
        val timeOneHourAgo = timeNow - (60 * 60 * 1000)

        val dummyExams = listOf(
            ExamItem("e1", "اختبار الوحدة الثانية", "مادة التربية الإسلامية", "", timeNow, ExamStatus.NOT_COMPLETED),
            ExamItem("e2", "اختبار الوحدة الثالثة", "مادة اللغة العربية", "", timeOneHourAgo, ExamStatus.COMPLETED)
        )

        val dummyAssignments = listOf(
            AssignmentItem(
                id = "a1",
                title = "حل تمارين الدرس الأول",
                subjectName = "مادة الرياضيات",
                imageUrl = "",
                dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
                remainingTime = "متبقي 4 أيام",
                status = AssignmentStatus.PENDING
            ),
            AssignmentItem(
                id = "a2",
                title = "حل تمارين الدرس الثاني",
                subjectName = "مادة الرياضيات",
                imageUrl = "",
                dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
                remainingTime = "متبقي 4 أيام",
                status = AssignmentStatus.SUBMITTED
            ),
            AssignmentItem(
                id = "a2",
                title = "حل تمارين الدرس الثاني",
                subjectName = "مادة الرياضيات",
                imageUrl = "",
                dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
                remainingTime = "متبقي 4 أيام",
                status = AssignmentStatus.EXPIRED
            ),
            AssignmentItem(
                id = "a2",
                title = "حل تمارين الدرس الثاني",
                subjectName = "مادة الرياضيات",
                imageUrl = "",
                dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
                remainingTime = "متبقي 4 أيام",
                status = AssignmentStatus.LATE
            )
        )

        _state.value = TasksState(
            isLoading = false,
            examsByDate = mapOf("25 / 8 / 2025، الثلاثاء" to dummyExams),
            assignmentsByDate = mapOf("25 / 8 / 2025، الثلاثاء" to dummyAssignments)
        )
    }
}