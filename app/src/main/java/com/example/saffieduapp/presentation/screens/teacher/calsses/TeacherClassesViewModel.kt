package com.example.saffieduapp.presentation.screens.teacher.calsses


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class TeacherClassesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(TeacherClassesState())
    val state = _state.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val currentEmail = auth.currentUser?.email ?: return@launch

                // 🔹 الخطوة 1: جلب teacherId من مجموعة "teachers"
                val teacherQuery = firestore.collection("teachers")
                    .whereEqualTo("email", currentEmail)
                    .get()
                    .await()

                if (teacherQuery.isEmpty) {
                    _state.value = _state.value.copy(isLoading = false, classes = emptyList())
                    return@launch
                }

                val teacherId = teacherQuery.documents.first().id

                // 🔹 الخطوة 2: جلب المواد التي يدرّسها هذا المعلم من مجموعة "subjects"
                val subjectsSnapshot = firestore.collection("subjects")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()

                val classesList = subjectsSnapshot.documents.map { doc ->
                    ClassItem(
                        classId = doc.id,
                        className = doc.getString("className") ?: "غير معروف",
                        subjectName = doc.getString("subjectName") ?: "بدون اسم",
                        subjectImageUrl = doc.getString("subjectImageUrl") ?: "",
                        quizCount = (doc.getLong("quizCount") ?: 0).toInt(),
                        assignmentCount = (doc.getLong("assignmentCount") ?: 0).toInt(),
                        videoLessonCount = (doc.getLong("videoLessonCount") ?: 0).toInt(),
                        pdfLessonCount = (doc.getLong("pdfLessonCount") ?: 0).toInt(),
                        studentCount = (doc.getLong("studentCount") ?: 0).toInt()
                    )
                }

                _state.value = TeacherClassesState(isLoading = false, classes = classesList)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, classes = emptyList())
            }
        }
    }
}