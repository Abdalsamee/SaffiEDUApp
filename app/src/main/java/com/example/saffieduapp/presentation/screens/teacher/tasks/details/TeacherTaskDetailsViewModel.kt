package com.example.saffieduapp.presentation.screens.teacher.tasks.details

// ... (بقية الـ Imports) ...
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TaskType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeacherTaskDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val taskId: String = checkNotNull(savedStateHandle["taskId"])
    private val taskTypeString: String = checkNotNull(savedStateHandle["taskType"])

    private val _taskType: TaskType = TaskType.valueOf(taskTypeString)
    val taskType: TaskType get() = _taskType  // 👈 getter عام للاستخدام في الشاشة

    private val _state = MutableStateFlow(TeacherTaskDetailsState())
    val state: StateFlow<TeacherTaskDetailsState> = _state

    init {
        loadStudentSubmissions()
    }

    private fun loadStudentSubmissions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1. تحديد اسم مجموعة التقديمات (Submissions Collection)
            val submissionCollectionName = when (taskType) {
                TaskType.ASSIGNMENT -> "assignment_submissions"
                TaskType.EXAM -> "exam_submissions"
            }

            // 2. تحديد اسم الحقل لمعرّف المهمة في مستند التقديم
            val taskIdFieldName = when (taskType) {
                TaskType.ASSIGNMENT -> "assignmentId"
                TaskType.EXAM -> "examId"
            }

            try {
                // 3. جلب جميع التقديمات للمهمة المحددة
                // أي مستند يتم جلبه هنا يعتبر تسليماً
                val submissionsSnapshot =
                    db.collection(submissionCollectionName).whereEqualTo(taskIdFieldName, taskId)
                        .get().await()

                val studentItems = mutableListOf<StudentTaskItem>()

                // 4. المرور على كل تقديم وجلب بيانات الطالب المقابلة
                for (submissionDoc in submissionsSnapshot.documents) {
                    val studentId = submissionDoc.getString("studentId")

                    if (studentId != null) {
                        // 5. جلب بيانات الطالب من مجموعة 'students'
                        val studentDoc = db.collection("students").document(studentId).get().await()

                        val studentName = studentDoc.getString("fullName") ?: "اسم غير معروف"
                        val studentImageUrl = studentDoc.getString("profileImageUrl") ?: ""

                        // 6. تحديد النتيجة أو الحالة
                        val scoreOrStatus = when (taskType) {
                            TaskType.ASSIGNMENT -> {
                                // لحالة الواجب: نفترض أنه تم التسليم بمجرد وجود المستند
                                "تم التسليم"
                            }

                            TaskType.EXAM -> {
                                // لحالة الاختبار: نفترض أنه تم التسليم بمجرد وجود المستند، ونبحث عن النتيجة
                                val score = submissionDoc.getLong("score")
                                val maxScore = submissionDoc.getLong("maxScore")

                                if (score != null && maxScore != null) {
                                    // حالة: النتيجة متوفرة
                                    "$score / $maxScore"
                                } else {
                                    // حالة: تم التسليم، لكن النتيجة غير متوفرة بعد (في انتظار التصحيح)
                                    "تم الانهاء (لم يصحح)"
                                }
                            }
                        }

                        studentItems.add(
                            StudentTaskItem(
                                id = studentId,
                                name = studentName,
                                score = scoreOrStatus,
                                imageUrl = studentImageUrl
                            )
                        )
                    }
                }

                _state.value = _state.value.copy(
                    isLoading = false, students = studentItems
                )

            } catch (e: Exception) {
                // معالجة الخطأ
                println("Error loading student submissions: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, students = emptyList())
            }
        }
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}