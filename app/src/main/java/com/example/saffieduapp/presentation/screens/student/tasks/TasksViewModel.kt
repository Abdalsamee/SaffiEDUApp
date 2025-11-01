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
        db.collection("students").whereEqualTo("email", email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val studentId = document.id
                    onResult(studentId)
                } else {
                    onResult(null)
                }
            }.addOnFailureListener {
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
                            isLoading = false, assignmentsByDate = assignmentsByDate, error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false, error = "تعذر جلب صف الطالب"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isLoading = false, error = "فشل في تحميل البيانات"
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
                    // ✅ Pass studentId to the next function
                    val exams = getExamsByClass(studentClass, studentId)
                    val examsByDate = groupExamsByDate(exams)

                    _state.update {
                        it.copy(
                            examsByDate = examsByDate, isLoading = false, error = null
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        error = "فشل في تحميل الاختبارات", isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun getExamsByClass(
        studentClass: String, studentId: String
    ): List<ExamItem> { // ✅ Added studentId
        return try {
            val snapshot =
                db.collection("exams").whereEqualTo("className", studentClass).get().await()

            // We must now process each document one by one to check its submission status
            val examItems = mutableListOf<ExamItem>()

            for (doc in snapshot.documents) {
                // ✅ For each exam, check if this specific student has submitted it
                val hasSubmitted = hasStudentSubmittedExam(doc.id, studentId)

                val showResults = doc.getBoolean("showResultsImmediately") ?: false

                val item = ExamItem(
                    id = doc.id,
                    title = doc.getString("examTitle") ?: "بدون عنوان",
                    subjectName = doc.getString("examType") ?: "عام",
                    imageUrl = "",
                    time = parseExamDateTime(doc),
                    status = determineExamStatus(doc, hasSubmitted),
                    showResultsImmediately = showResults
                )
                examItems.add(item)
            }
            examItems // Return the list of items with correct statuses

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

    private fun determineExamStatus(doc: DocumentSnapshot, hasSubmitted: Boolean): ExamStatus {
        // 1. إذا كان الطالب قد سلّم، فالحالة "مكتمل" دائماً
        if (hasSubmitted) {
            return ExamStatus.COMPLETED
        }

        // 2. إذا لم يسلّم الطالب، نتحقق من الوقت
        return try {
            val examStartTime = parseExamDateTime(doc)
            val currentTime = System.currentTimeMillis()
            val examDurationMillis = (doc.getString("examTime")?.toIntOrNull()
                ?: 60) * 60 * 1000 // مدة الاختبار بالمللي ثانية
            val examEndTime = examStartTime + examDurationMillis

            when {
                // الاختبار لم يبدأ بعد (في المستقبل)
                currentTime < examStartTime -> ExamStatus.NOT_COMPLETED

                // الاختبار متاح الآن (ولم يسلّمه الطالب)
                currentTime in examStartTime..examEndTime -> ExamStatus.IN_PROGRESS

                // انتهى وقت الاختبار (ولم يسلّمه الطالب)
                // ✅ هذا هو التغيير الذي طلبته
                else -> ExamStatus.COMPLETED // <-- تم تغييرها من NOT_COMPLETED
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

    private suspend fun hasStudentSubmittedExam(examId: String, studentId: String): Boolean {
        return try {
            val submissionQuery = db.collection("exam_submissions").whereEqualTo("examId", examId)
                .whereEqualTo("studentId", studentId)
                .limit(1) // We only care if at least one submission exists
                .get().await()

            // If the query is NOT empty, it means a submission exists.
            !submissionQuery.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false // Assume no submission on error
        }
    }

}