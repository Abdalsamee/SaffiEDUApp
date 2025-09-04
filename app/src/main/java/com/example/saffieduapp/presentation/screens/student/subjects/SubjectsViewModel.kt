package com.example.saffieduapp.presentation.screens.student.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectsState())
    val state = _state.asStateFlow()

    init {
        refresh() // تحميل البيانات مباشرة عند إنشاء ViewModel
    }

    /**
     * جلب البيانات من Firestore مع تحديد أقصى وقت 3 ثواني.
     * يُستخدم أيضًا عند سحب الشاشة لتحديث البيانات.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                withTimeout(3000) { // أقصى وقت لجلب البيانات
                    loadSubjects()
                }
            } catch (e: Exception) {
                // في حالة الخطأ، نترك الشاشة بدون تعليق
            } finally {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * تحميل المواد من Firestore.
     */
    private suspend fun loadSubjects() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val querySnapshot = firestore.collection("subjects").get().await()
            val subjectsList = querySnapshot.documents.mapNotNull { doc ->
                val subjectName = doc.getString("subjectName") ?: return@mapNotNull null
                val teacherName = doc.getString("teacherName") ?: "غير معروف"
                val grade = doc.getString("className") ?: "غير محدد"
                val lessonsCount = (doc.getLong("lessonsCount") ?: 0).toInt()
                val rating = (doc.getDouble("rating") ?: 0.0).toFloat()

                Subject(
                    id = doc.id,
                    name = subjectName,
                    teacherName = teacherName,
                    grade = grade,
                    rating = rating,
                    imageUrl = "",
                    totalLessons = lessonsCount
                )
            }

            _state.value = SubjectsState(isLoading = false, subject = subjectsList)
        } catch (e: Exception) {
            _state.value = SubjectsState(isLoading = false, subject = emptyList())
        }
    }

    /**
     * تحديث تقييم المادة محليًا
     */
    fun updateRating(subjectId: String, newRating: Int) {
        val currentSubjects = _state.value.subject.toMutableList()
        val index = currentSubjects.indexOfFirst { it.id == subjectId }
        if (index != -1) {
            currentSubjects[index] = currentSubjects[index].copy(rating = newRating.toFloat())
            _state.value = _state.value.copy(subject = currentSubjects)
        }
    }
}
