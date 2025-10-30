package com.example.saffieduapp.presentation.screens.teacher.tasks.details

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

    // استخراج معرّف المهمة ونوعها من المسار
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    // يجب أن تتأكد أن taskType يتم تمريره كسلسلة قابلة للتحويل إلى TaskType (مثل "ASSIGNMENT" أو "EXAM")
    private val taskTypeString: String = checkNotNull(savedStateHandle["taskType"])
    private val taskType: TaskType = TaskType.valueOf(taskTypeString)

    private val _state = MutableStateFlow(TeacherTaskDetailsState())
    val state: StateFlow<TeacherTaskDetailsState> = _state

    init {
        // بدلاً من تحميل البيانات الوهمية، سنقوم بتحميل البيانات من Firestore
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
                val submissionsSnapshot = db.collection(submissionCollectionName)
                    .whereEqualTo(taskIdFieldName, taskId)
                    .get()
                    .await()

                val studentItems = mutableListOf<StudentTaskItem>()

                // 4. المرور على كل تقديم وجلب بيانات الطالب المقابلة
                for (submissionDoc in submissionsSnapshot.documents) {
                    val studentId = submissionDoc.getString("studentId")

                    if (studentId != null) {
                        // 5. جلب بيانات الطالب من مجموعة 'students'
                        val studentDoc = db.collection("students").document(studentId)
                            .get()
                            .await()

                        val studentName = studentDoc.getString("fullName") ?: "اسم غير معروف"
                        val studentImageUrl = studentDoc.getString("profileImageUrl") ?: ""

                        // 6. تحديد النتيجة أو الحالة
                        val scoreOrStatus = when (taskType) {
                            TaskType.ASSIGNMENT -> {
                                // لحالة الواجب، نتحقق فقط مما إذا كان قد تم التقديم (submitted)
                                val submitted = submissionDoc.getBoolean("submitted") ?: false
                                if (submitted) "تم التسليم" else "لم يسلم" // يمكنك تعديل هذا ليعرض التقدير لاحقاً إذا كان موجوداً
                            }

                            TaskType.EXAM -> {
                                // لحالة الاختبار، نتحقق من حقل النتيجة (Score) إذا كان متاحاً، أو نستخدم حالة
                                val isSubmitted = submissionDoc.getBoolean("submitted") ?: false
                                // إذا كان لديك حقل 'score' في مستندات الاختبارات:
                                // val score = submissionDoc.getLong("score")
                                if (isSubmitted) "تم الانهاء" else "لم يبدأ" // Placeholder. عدّله لجلب النتيجة الفعلية عند توفرها
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
                    isLoading = false,
                    students = studentItems
                )

            } catch (e: Exception) {
                // يمكنك إضافة معالجة أفضل للخطأ هنا
                println("Error loading student submissions: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, students = emptyList())
            }
        }
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}
