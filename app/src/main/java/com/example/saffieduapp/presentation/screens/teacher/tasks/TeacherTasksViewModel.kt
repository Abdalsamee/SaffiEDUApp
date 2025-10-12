package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherTasksViewModel : ViewModel() {

    private val _state = MutableStateFlow(TeacherTasksState())
    val state: StateFlow<TeacherTasksState> = _state

    init {
        // تحميل بيانات وهمية عند بدء الشاشة
        loadFakeData()
    }

    private fun loadFakeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val fakeAssignments = listOf(
                TeacherTaskItem(
                    id = "a1",
                    subject = "اللغة العربية",
                    date = "22/9/2025",
                    time = "12:00 am",
                    isActive = true,
                    type = TaskType.ASSIGNMENT
                ),
                TeacherTaskItem(
                    id = "a2",
                    subject = "الرياضيات",
                    date = "23/9/2025",
                    time = "10:00 am",
                    isActive = false,
                    type = TaskType.ASSIGNMENT
                )
            )

            val fakeExams = listOf(
                TeacherTaskItem(
                    id = "e1",
                    subject = "اللغة العربية",
                    date = "25/9/2025",
                    time = "11:00 am",
                    isActive = false,
                    type = TaskType.EXAM
                ),
                TeacherTaskItem(
                    id = "e2",
                    subject = "العلوم",
                    date = "26/9/2025",
                    time = "09:00 am",
                    isActive = true,
                    type = TaskType.EXAM
                )
            )

            _state.value = _state.value.copy(
                assignments = fakeAssignments,
                exams = fakeExams,
                isLoading = false
            )
        }
    }

    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    fun onClassSelected(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
    }
}
