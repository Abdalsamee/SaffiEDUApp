package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherTasksViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(TeacherTasksState())
    val state: StateFlow<TeacherTasksState> = _state

    init {
        loadAssignments()
        loadExams()
    }

    private fun loadAssignments(selectedClass: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            var query: Query = db.collection("assignments")
            if (!selectedClass.isNullOrEmpty()) {
                query = query.whereEqualTo(
                    "className",
                    selectedClass
                ) // تأكد أن حقل الصف اسمه className في Firestore
            }
            query.get()
                .addOnSuccessListener { snapshot ->
                    val assignments = snapshot.documents.map { doc ->
                        TeacherTaskItem(
                            id = doc.id,
                            subject = doc.getString("subjectName") ?: "",
                            date = doc.getString("dueDate") ?: "",
                            time = "23:59",
                            isActive = true,
                            type = TaskType.ASSIGNMENT,
                            title = doc.getString("title")
                        )
                    }
                    _state.value = _state.value.copy(assignments = assignments, isLoading = false)
                }
                .addOnFailureListener {
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }

    private fun loadExams(selectedClass: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            var query: Query = db.collection("exams")
            if (!selectedClass.isNullOrEmpty()) {
                query = query.whereEqualTo(
                    "className",
                    selectedClass
                ) // تأكد أن حقل الصف اسمه className في Firestore
            }
            query.get()
                .addOnSuccessListener { snapshot ->
                    val exams = snapshot.documents.map { doc ->
                        TeacherTaskItem(
                            id = doc.id,
                            subject = doc.getString("subjectName") ?: "",
                            date = doc.getString("examDate") ?: "",
                            time = doc.getString("examStartTime") ?: "",
                            isActive = true,
                            type = TaskType.EXAM,
                            title = doc.getString("title")
                        )
                    }
                    _state.value = _state.value.copy(exams = exams, isLoading = false)
                }
                .addOnFailureListener {
                    _state.value = _state.value.copy(isLoading = false)
                }
        }
    }


    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    fun onClassSelected(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        loadAssignments(selectedClass = className)
        loadExams(selectedClass = className)
    }
}
