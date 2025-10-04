package com.example.saffieduapp.presentation.screens.teacher.calsses


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    fun refreshClasses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            // تأخير 2 ثانية
            kotlinx.coroutines.delay(2000)
            loadClasses() // إعادة تحميل البيانات
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val currentEmail = auth.currentUser?.email ?: return@launch

                // جلب teacherId من مجموعة "teachers"
                val teacherQuery = firestore.collection("teachers")
                    .whereEqualTo("email", currentEmail)
                    .get()
                    .await()

                if (teacherQuery.isEmpty) {
                    _state.value = _state.value.copy(isLoading = false, classes = emptyList())
                    return@launch
                }

                val teacherId = teacherQuery.documents.first().id

                // جلب المواد التي يدرّسها المعلم
                val subjectsSnapshot = firestore.collection("subjects")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()

                val classesList = subjectsSnapshot.documents.map { doc ->
                    async {
                        val className = doc.getString("className") ?: "غير معروف"
                        val subjectName = doc.getString("subjectName") ?: "بدون اسم"

                        // جلب جميع الدروس لهذا الصف والمادة
                        val lessonsSnapshot = firestore.collection("lessons")
                            .whereEqualTo("className", className)
                            .whereEqualTo("subjectName", subjectName)
                            .get()
                            .await()

                        var videoCount = 0
                        var pdfCount = 0
                        lessonsSnapshot.documents.forEach { lesson ->
                            if (!lesson.getString("videoUrl").isNullOrEmpty()) videoCount++
                            if (!lesson.getString("pdfUrl").isNullOrEmpty()) pdfCount++
                        }

                        val assignmentCount = getAssignmentsCountForClass(subjectName, className)

                        ClassItem(
                            classId = doc.id,
                            className = className,
                            subjectName = subjectName,
                            subjectImageUrl = doc.getString("subjectImageUrl") ?: "",
                            quizCount = (doc.getLong("quizCount") ?: 0).toInt(),
                            assignmentCount = assignmentCount,
                            videoLessonCount = videoCount,
                            pdfLessonCount = pdfCount,
                            studentCount = (doc.getLong("studentCount") ?: 0).toInt()
                        )
                    }
                }.awaitAll()

                _state.value = TeacherClassesState(isLoading = false, classes = classesList)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false, classes = emptyList())
            }
        }
    }

    private suspend fun getAssignmentsCountForClass(subjectName: String, className: String): Int {
        return try {
            val snapshot = firestore.collection("assignments")
                .whereEqualTo("subjectName", subjectName)
                .whereEqualTo("className", className)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}