package com.example.saffieduapp.presentation.screens.student.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.repository.AssignmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
                    // ✅ تحميل الواجبات فقط
                    loadAssignments(studentId)
                    // ✅ تحميل الاختبارات فقط
                    loadExams(studentId)
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

    fun loadAssignments(studentId: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // جلب الصف الحالي للطالب
                val studentClass = assignmentRepository.getStudentClass(studentId)

                if (studentClass != null) {
                    // جلب الواجبات للصف المحدد فقط
                    val assignments = assignmentRepository.getAllAssignments(studentClass)
                    val assignmentsByDate = groupAssignmentsByDate(assignments)

                    _state.update {
                        it.copy(
                            isLoading = false,
                            assignmentsByDate = assignmentsByDate,
                            error = null
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
        val format = SimpleDateFormat("yyyy-MM-dd، EEEE", Locale.ENGLISH)
        return format.format(date)
    }

    fun loadExams(studentId: String) {
        viewModelScope.launch {
            try {
                val studentClass = assignmentRepository.getStudentClass(studentId)
                if (studentClass != null) {
                    val exams = getExamsByClass(studentClass)
                    val examsByDate = groupExamsByDate(exams) // ✅ استخدام الدالة المحسنة

                    _state.update {
                        it.copy(
                            examsByDate = examsByDate,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        error = "فشل في تحميل الاختبارات",
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun getExamsByClass(studentClass: String): List<ExamItem> {
        return try {
            val snapshot = db.collection("exams")
                .whereEqualTo("className", studentClass)
                .get()
                .await()

            snapshot.documents.map { doc ->
                ExamItem(
                    id = doc.id,
                    title = doc.getString("examTitle") ?: "بدون عنوان",
                    subjectName = doc.getString("examType") ?: "عام",
                    imageUrl = "",
                    time = parseExamDateTime(doc),
                    status = determineExamStatus(doc)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    private fun parseExamDateTime(doc: DocumentSnapshot): Long {
        return try {
            // قراءة التاريخ والوقت من Firebase
            val examDate = doc.getString("examDate") ?: return System.currentTimeMillis()
            val examStartTime = doc.getString("examStartTime") ?: "00:00"

            // دمج التاريخ والوقت
            val dateTimeString = "$examDate $examStartTime"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)

            sdf.parse(dateTimeString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            e.printStackTrace()
            System.currentTimeMillis()
        }
    }

    private fun determineExamStatus(doc: DocumentSnapshot): ExamStatus {
        return try {
            val examTime = parseExamDateTime(doc)
            val currentTime = System.currentTimeMillis()
            val examDuration = (doc.getString("examTime")?.toIntOrNull() ?: 60) * 60 * 1000 // تحويل الدقائق إلى مللي ثانية

            when {
                currentTime < examTime -> ExamStatus.NOT_COMPLETED
                currentTime > examTime + examDuration -> ExamStatus.COMPLETED
                else -> ExamStatus.IN_PROGRESS
            }
        } catch (e: Exception) {
            ExamStatus.NOT_COMPLETED
        }
    }
    private fun groupExamsByDate(exams: List<ExamItem>): Map<String, List<ExamItem>> {
        return exams.groupBy { exam ->
            // استخدام وقت الاختبار الفعلي بدلاً من الوقت الحالي
            formatDateForGrouping(Date(exam.time))
        }
    }

    fun getAssignmentById(id: String): AssignmentItem? {
        return state.value.assignmentsByDate.values.flatten().find { it.id == id }
    }

    fun getExamById(id: String): ExamItem? {
        return state.value.examsByDate.values.flatten().find { it.id == id }
    }

}