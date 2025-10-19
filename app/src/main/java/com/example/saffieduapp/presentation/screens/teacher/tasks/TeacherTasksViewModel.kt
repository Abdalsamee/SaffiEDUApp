package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TeacherTasksViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserEmail = auth.currentUser?.email

    private var teacherId: String? = null // سيتم تخزين معرف المعلم بعد الجلب الأول

    private val _state = MutableStateFlow(TeacherTasksState())
    val state: StateFlow<TeacherTasksState> = _state

    init {
        fetchTeacherIdAndLoadTasks() // تحميل المهام عند بدء التشغيل
    }

    // جلب معرف المعلم من Firestore باستخدام البريد الإلكتروني
    private fun fetchTeacherIdAndLoadTasks(selectedClass: String? = null) {
        // إذا كان المعرف موجود مسبقًا، نعيد تحميل المهام فقط
        teacherId?.let {
            loadAssignments(it, selectedClass)
            loadExams(it, selectedClass)
            return
        }

        // إذا لم يوجد بريد المستخدم
        if (currentUserEmail == null) return

        _state.value = _state.value.copy(isLoading = true)

        db.collection("teachers").whereEqualTo("email", currentUserEmail).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val teacherDoc = snapshot.documents[0]
                    teacherId = teacherDoc.id
                    loadAssignments(teacherId!!, selectedClass)
                    loadExams(teacherId!!, selectedClass)
                } else {
                    // لم يتم العثور على المعلم
                    _state.value = _state.value.copy(
                        assignments = emptyList(), exams = emptyList(), isLoading = false
                    )
                }
            }.addOnFailureListener {
                _state.value = _state.value.copy(isLoading = false)
            }
    }

    // تحميل الواجبات الخاصة بالمعلم حسب الصف
    private fun loadAssignments(teacherId: String, selectedClass: String? = null) {
        _state.value = _state.value.copy(isLoading = true)

        var query: Query = db.collection("assignments").whereEqualTo("teacherId", teacherId)

        if (!selectedClass.isNullOrEmpty()) {
            query = query.whereEqualTo("className", selectedClass)
        }

        query.get().addOnSuccessListener { snapshot ->
                val assignments = snapshot.documents.map { doc ->
                    TeacherTaskItem(
                        id = doc.id,
                        subject = doc.getString("subjectName") ?: "",
                        date = doc.getString("dueDate") ?: "",
                        time = doc.getString("dueTime") ?: "23:59",
                        isActive = true,
                        type = TaskType.ASSIGNMENT,
                        title = doc.getString("title")
                    )
                }
                _state.value = _state.value.copy(assignments = assignments, isLoading = false)
            }.addOnFailureListener {
                _state.value = _state.value.copy(isLoading = false)
            }
    }

    // تحميل الاختبارات الخاصة بالمعلم حسب الصف
    private fun loadExams(teacherId: String, selectedClass: String? = null) {
        _state.value = _state.value.copy(isLoading = true)

        var query: Query = db.collection("exams").whereEqualTo("teacherId", teacherId)

        if (!selectedClass.isNullOrEmpty()) {
            query = query.whereEqualTo("className", selectedClass)
        }

        query.get().addOnSuccessListener { snapshot ->
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
            }.addOnFailureListener {
                _state.value = _state.value.copy(isLoading = false)
            }
    }

    // تغيير التبويب (واجبات / اختبارات)
    fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    // تغيير الصف وإعادة تحميل المهام والاختبارات
    fun onClassSelected(className: String) {
        _state.value = _state.value.copy(selectedClass = className)
        fetchTeacherIdAndLoadTasks(className)
    }

}