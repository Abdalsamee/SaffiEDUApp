package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeacherTasksViewModel : ViewModel() {

    private val _state = MutableStateFlow(TeacherTasksState())
    val state = _state.asStateFlow()

    // الدالة التي تغيّر التبويب عند الضغط
    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }
}
