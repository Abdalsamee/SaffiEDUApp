package com.example.saffieduapp.presentation.screens.student.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.model.Subject
import com.google.firebase.auth.FirebaseAuth
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
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectsState(error = "خطأ في تحميل بيانات الطالب"))
    val state = _state.asStateFlow()

    // تخزين صف الطالب بعد جلبها
    private var studentGrade: String? = null


        init {
            loadUserData() //
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                withTimeout(5000) {
                    // أولاً: جلب بيانات الطالب لمعرفة صفه
                    // ثانياً: جلب المواد حسب صف الطالب
                    loadSubjectsByGrade()
                }
            } catch (e: Exception) {
                // معالجة الخطأ
            } finally {
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }
    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val currentUserEmail = auth.currentUser?.email // نفس الطريقة

            if (currentUserEmail != null) {
                try {
                    // نفس الكود الذي يعمل في HomeViewModel
                    val querySnapshot = firestore.collection("students")
                        .whereEqualTo("email", currentUserEmail) // البحث بالإيميل
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val studentDoc = querySnapshot.documents[0]
                        studentGrade = studentDoc.getString("grade") ?: ""

                        // بعد معرفة الصف، جلب المواد
                        loadSubjectsByGrade()
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        error = "خطأ في تحميل بيانات الطالب"
                    )
                }
            }
        }
    }
    /**
     * تحميل المواد من Firestore.
     */
    private suspend fun loadSubjectsByGrade() {
        try {

            val query = if (studentGrade!!.isNotEmpty()) {
                firestore.collection("subjects")
                    .whereEqualTo("className", studentGrade!!.trim())
                    .get()
                    .await()
            } else {
                firestore.collection("subjects").get().await()
            }

            val subjectsList = query.documents.mapNotNull { doc ->
                val subjectName = doc.getString("subjectName") ?: return@mapNotNull null
                val teacherName = doc.getString("teacherName") ?: "غير معروف"
                val grade = doc.getString("className") ?: "غير محدد"
                val lessonsCount = (doc.getLong("lessonsCount") ?: 0).toInt()
                val rating = (doc.getDouble("rating") ?: 0.0).toFloat()

                // ✅ تحويل اسم المعلم لعرض الأول والأخير فقط
                val formattedUserName = formatUserName(teacherName)

                Subject(
                    id = doc.id,
                    name = subjectName,
                    teacherName = formattedUserName,
                    grade = grade,
                    rating = rating,
                    imageUrl = "",
                    totalLessons = lessonsCount
                )
            }

            _state.value = SubjectsState(
                isLoading = false,
                subject = subjectsList,
                error = "خطأ في تحميل بيانات الطالب"
            )
        } catch (e: Exception) {
            _state.value = SubjectsState(isLoading = false, error = "خطأ في تحميل بيانات الطالب")
        }
    }

    /**
     * تحديث التقييم محلياً + تخزينه في Firestore
     */
    fun updateRating(subjectId: String, newRating: Int) {
        // تحديث محلي
        val currentSubjects = _state.value.subject.toMutableList()
        val index = currentSubjects.indexOfFirst { it.id == subjectId }
        if (index != -1) {
            currentSubjects[index] = currentSubjects[index].copy(rating = newRating.toFloat())
            _state.value = _state.value.copy(subject = currentSubjects)
        }

        // تحديث في Firestore
        viewModelScope.launch {
            try {
                firestore.collection("subjects")
                    .document(subjectId)
                    .update("rating", newRating)
                    .await()
            } catch (e: Exception) {
                // ممكن نضيف Log أو رسالة خطأ
            }
        }
    }
     fun formatUserName(fullName: String): String {
        return try {
            val nameParts = fullName.trim().split("\\s+".toRegex())

            when {
                nameParts.isEmpty() -> "غير معروف"
                nameParts.size == 1 -> nameParts[0] // اسم واحد فقط
                else -> {
                    val firstName = nameParts[0]
                    val lastName = nameParts[nameParts.size - 1]
                    "$firstName $lastName"
                }
            }
        } catch (e: Exception) {
            "غير معروف"
        }
    }
}