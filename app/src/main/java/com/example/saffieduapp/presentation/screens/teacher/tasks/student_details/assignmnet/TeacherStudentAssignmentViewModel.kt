package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.assignmnet

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class TeacherStudentAssignmentViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(TeacherStudentAssignmentState())
    val state = _state.asStateFlow()

    private var currentSubmissionDocId: String? = null // 🔹 نحتفظ بمعرّف المستند لتحديثه لاحقًا


    fun loadStudentAssignmentDetails(studentId: String, assignmentId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                // 🔹 البحث عن تسليم الطالب
                val querySnapshot =
                    db.collection("assignment_submissions").whereEqualTo("studentId", studentId)
                        .whereEqualTo("assignmentId", assignmentId).get().await()

                if (querySnapshot.isEmpty) {
                    _state.value = _state.value.copy(
                        isLoading = false, errorMessage = "لا يوجد تسليم لهذا الطالب."
                    )
                    return@launch
                }

                val submissionDoc = querySnapshot.documents.first()
                currentSubmissionDocId = submissionDoc.id // 🟢 حفظ المعرف لتحديثه لاحقًا

                val data = submissionDoc.data ?: emptyMap<String, Any>()

                val submittedFiles = (data["submittedFiles"] as? List<*>)?.mapNotNull {
                    val url = it as? String
                    if (url != null) {
                        val isImage = url.endsWith(".jpg", true) || url.endsWith(
                            ".jpeg", true
                        ) || url.endsWith(".png", true)
                        SubmittedFile(
                            fileName = url.substringAfterLast("/"), fileUrl = url, isImage = isImage
                        )
                    } else null
                } ?: emptyList()

                val submitted = data["submitted"] as? Boolean ?: false
                val notes = data["notes"] as? String ?: ""
                val grade = data["grade"]?.toString() ?: ""

                // 🔹 جلب بيانات الطالب
                val studentDoc = db.collection("students").document(studentId).get().await()
                val studentName = studentDoc.getString("fullName") ?: "اسم غير معروف"
                val studentClass = studentDoc.getString("className") ?: "غير محدد"

                _state.value = _state.value.copy(
                    isLoading = false,
                    studentName = studentName,
                    studentClass = studentClass,
                    deliveryStatus = if (submitted) "تم التسليم" else "لم يتم التسليم",
                    submittedFiles = submittedFiles,
                    comment = notes,
                    grade = grade
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, errorMessage = "حدث خطأ أثناء تحميل البيانات: ${e.message}"
                )
            }
        }
    }

    fun onGradeChange(value: String) {
        _state.value = _state.value.copy(grade = value)
    }

    fun onCommentChange(value: String) {
        _state.value = _state.value.copy(comment = value)
    }


    fun onSaveEvaluation() {
        viewModelScope.launch {
            val docId = currentSubmissionDocId
            if (docId == null) {
                _state.value = _state.value.copy(errorMessage = "لم يتم العثور على مستند التسليم.")
                return@launch
            }

            try {
                _state.value = _state.value.copy(isLoading = true)

                val grade = _state.value.grade.trim()
                val comment = _state.value.comment.trim()

                // 🔹 تحديث الحقول في Firestore
                db.collection("assignment_submissions").document(docId).update(
                    mapOf(
                        "grade" to grade,
                        "notes" to comment,
                        "evaluated" to true // 🔸 يمكنك استخدام هذا الحقل للدلالة على أنه تم التقييم
                    )
                ).await()

                _state.value = _state.value.copy(
                    isLoading = false, errorMessage = null
                )

                println("✅ تم حفظ تقييم الطالب بنجاح في Firestore")

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, errorMessage = "فشل في حفظ التقييم: ${e.message}"
                )
            }
        }
    }
}
