package com.example.saffieduapp.presentation.screens.student.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.repository.AssignmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState(error = "فشل في تحميل البيانات"))
    val state = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        fetchCurrentStudentIdAndLoadTasks()
    }


    private fun fetchCurrentStudentIdAndLoadTasks() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchStudentIdByEmail(currentUser.email ?: "") { studentId ->
                if (studentId != null) {
                    loadTasks(studentId)
                } else {
                    _state.update { it.copy(error = "تعذر جلب بيانات الطالب") }
                }
            }
        } else {
            _state.update { it.copy(error = "الطالب غير مسجل الدخول") }
        }
    }

    private fun fetchStudentIdByEmail(email: String, onResult: (String?) -> Unit) {
        db.collection("students")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val studentId = document.id
                    onResult(studentId)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTabIndex = index) }
    }

    fun loadTasks(studentId: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // جلب الصف الحالي للطالب
                val studentClass = assignmentRepository.getStudentClass(studentId)

                if (studentClass != null) {
                    // جلب الواجبات للصف المحدد فقط
                    val assignments = assignmentRepository.getAllAssignments(studentClass)
                    val assignmentsByDate = groupAssignmentsByDate(assignments)

                    val examsByDate = getDummyExams() // لاحقاً يمكن فلترة الاختبارات حسب الصف أيضاً

                    _state.update {
                        it.copy(
                            isLoading = false,
                            assignmentsByDate = assignmentsByDate,
                            examsByDate = examsByDate
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "تعذر جلب صف الطالب"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "فشل في تحميل البيانات"
                    )
                }
            }
        }
    }

    private fun groupAssignmentsByDate(assignments: List<AssignmentItem>): Map<String, List<AssignmentItem>> {
        return assignments.groupBy { assignment ->
            // استخدام التاريخ الحالي كنموذج، يمكنك تعديله حسب حاجتك
            formatDateForGrouping(Calendar.getInstance().time)
        }
    }

    private fun formatDateForGrouping(date: Date): String {
        val format = SimpleDateFormat("dd / MM / yyyy، EEEE", Locale("ar"))
        return format.format(date)
    }

    private fun getDummyExams(): Map<String, List<ExamItem>> {
        val timeNow = Calendar.getInstance().timeInMillis
        val timeOneHourAgo = timeNow - (60 * 60 * 1000)

        val dummyExams = listOf(
            ExamItem("e1", "اختبار الوحدة الثانية", "مادة التربية الإسلامية", "", timeNow, ExamStatus.NOT_COMPLETED),
            ExamItem("e2", "اختبار الوحدة الثالثة", "مادة اللغة العربية", "", timeOneHourAgo, ExamStatus.COMPLETED)
        )

        return mapOf(formatDateForGrouping(Calendar.getInstance().time) to dummyExams)
    }
}