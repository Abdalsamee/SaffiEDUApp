package com.example.saffieduapp.presentation.screens.student.assignment_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StudentAssignmentResultViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StudentAssignmentResultState(isLoading = true))
    val state: StateFlow<StudentAssignmentResultState> = _state

    /**
     * تحميل نتيجة واجب معين بناءً على الـ assignmentId
     */
    fun loadResultData(assignmentId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // 🔹 البحث عن الوثيقة التي تحتوي نفس assignmentId
                val querySnapshot = firestore.collection("assignment_submissions")
                    .whereEqualTo("assignmentId", assignmentId)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "لم يتم العثور على النتيجة لهذا الواجب."
                        )
                    }
                    return@launch
                }

                val doc = querySnapshot.documents.first()
                val grade = doc.getString("grade") ?: ""
                val comment = doc.getString("notes") ?: ""
                val studentId = doc.getString("studentId") ?: ""
                val submittedFiles = doc.get("submittedFiles") as? List<String> ?: emptyList()

                // ✅ جلب اسم الطالب من مجموعة students
                val studentDoc = firestore.collection("students")
                    .document(studentId)
                    .get()
                    .await()

                val studentName = studentDoc.getString("fullName") ?: "غير معروف"

                // ✅ تحديث الحالة بالبيانات
                _state.update {
                    it.copy(
                        isLoading = false,
                        assignmentTitle = "واجب رقم ${assignmentId.takeLast(4)}",
                        studentName = studentName,
                        files = submittedFiles,
                        grade = grade,
                        comment = comment,
                        errorMessage = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "حدث خطأ أثناء تحميل البيانات: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
