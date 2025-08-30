package com.example.saffieduapp.presentation.screens.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.FeaturedLesson
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.domain.model.UrgentTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.withTimeout

data class StdData(
    val fullName: String = "",
    val grade: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadUserData()
        viewModelScope.launch {
            loadInitialData()
            loadTeachersSubjects()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val currentUserEmail = auth.currentUser?.email
            if (currentUserEmail != null) {
                try {
                    val querySnapshot = firestore.collection("students")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val userData = querySnapshot.documents[0].toObject(StdData::class.java)
                        if (userData != null) {
                            val nameParts = userData.fullName.trim().split("\\s+".toRegex())
                            val firstName = nameParts.firstOrNull() ?: ""
                            val lastName = if (nameParts.size > 1) nameParts.last() else ""
                            val displayName = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName

                            _state.value = _state.value.copy(
                                studentName = displayName,
                                studentGrade = userData.grade
                            )
                        }
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        studentName = "خطأ في التحميل",
                        studentGrade = "خطأ في التحميل"
                    )
                }
            } else {
                _state.value = _state.value.copy(
                    studentName = "لم يتم تسجيل الدخول",
                    studentGrade = "لم يتم تسجيل الدخول"
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                // أقصى وقت لجلب البيانات: 3 ثواني
                withTimeout(3000) {
                    loadTeachersSubjects()
                    loadInitialData()
                }
            } catch (_: Exception) {
                // تجاهل الخطأ أو سجّله إذا لزم
            } finally {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    // suspend function → ما في launch داخله
    private suspend fun loadInitialData() {
        val urgentTasksList = listOf(
            UrgentTask("1", "اختبار نصفي", "التربية الإسلامية", "24/8/2025", "11 صباحاً", ""),
            UrgentTask("2", "المهمة رقم 1", "اللغة الانجليزية", "24/8/2025", "12 مساءً", "")
        )

        val lessonsList = listOf(
            FeaturedLesson("l1", "Romeo story", "English", "15 دقيقة", 30, ""),
            FeaturedLesson("l2", "درس الكسور", "رياضيات", "15 دقيقة", 80, "")
        )

        _state.value = _state.value.copy(
            urgentTasks = urgentTasksList,
            featuredLessons = lessonsList
        )
    }

    // suspend function → مباشرة تستخدم await
    private suspend fun loadTeachersSubjects() {
        try {
            val querySnapshot = firestore.collection("teachers").get().await()
            val subjectsList = querySnapshot.documents.map { doc ->
                val fullName = doc.getString("fullName") ?: "غير معروف"
                val subjectName = doc.getString("subject") ?: "غير معروف"
                Subject(
                    id = doc.id,
                    name = subjectName,
                    teacherName = fullName,
                    grade = "",
                    rating = 0f,
                    imageUrl = "",
                    totalLessons = 0
                )
            }

            _state.value = _state.value.copy(
                enrolledSubjects = subjectsList,
                isLoading = false
            )
        } catch (_: Exception) {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _state.value = _state.value.copy(searchQuery = newQuery)
    }
}
