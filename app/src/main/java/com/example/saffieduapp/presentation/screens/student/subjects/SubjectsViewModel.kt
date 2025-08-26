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
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectsState())
    val state = _state.asStateFlow()

    init {
        loadSubjects()
    }

    fun refresh() {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                // جلب جميع المدرسين والمواد التي يدرسونها من Firestore
                val querySnapshot = firestore.collection("teachers").get().await()
                val subjectsList = querySnapshot.documents.mapNotNull { doc ->
                    val teacherName = doc.getString("fullName") ?: return@mapNotNull null
                    val subjectName = doc.getString("subject") ?: return@mapNotNull null

                    Subject(
                        id = doc.id,
                        name = subjectName,
                        teacherName = teacherName,
                        grade = "الصف العاشر", // يمكن تعديلها حسب المرحلة أو جلبها من Firestore إذا متاحة
                        rating = 0f,
                        imageUrl = "", // ضع رابط الصورة إذا متاح في Firestore
                        lessonCount = 0 // عدد الدروس إذا متاح
                    )
                }

                _state.value = SubjectsState(isLoading = false, subject = subjectsList)
            } catch (e: Exception) {
                // في حالة الخطأ، نعطي قائمة فارغة
                _state.value = SubjectsState(isLoading = false, subject = emptyList())
            }
        }
    }

    fun updateRating(subjectId: String, newRating: Int) {
        val currentSubjects = _state.value.subject.toMutableList()
        val index = currentSubjects.indexOfFirst { it.id == subjectId }
        if (index != -1) {
            currentSubjects[index] = currentSubjects[index].copy(rating = newRating.toFloat())
            _state.value = _state.value.copy(subject = currentSubjects)
        }
    }
}
