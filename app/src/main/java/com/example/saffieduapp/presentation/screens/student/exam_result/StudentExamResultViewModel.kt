package com.example.saffieduapp.presentation.screens.student.exam_result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StudentExamResultViewModel @Inject constructor(
    val auth: FirebaseAuth, val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StudentExamResultState(isLoading = true))
    val state: StateFlow<StudentExamResultState> = _state

    /**
     * 🔹 تحميل بيانات النتيجة
     * لاحقاً: سيتم ربطها مع Firestore حسب examId و studentId
     */
    /**
     * 🔹 تحميل بيانات النتيجة من Firestore
     */
    fun loadExamResult(examId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _state.update {
                        it.copy(
                            isLoading = false, errorMessage = "الرجاء تسجيل الدخول أولاً."
                        )
                    }
                    return@launch
                }

                // 1. استخراج StudentId
                val studentDoc =
                    firestore.collection("students").whereEqualTo("email", currentUser.email).get()
                        .await().documents.firstOrNull()
                val studentId = studentDoc?.id

                if (studentId == null) {
                    _state.update {
                        it.copy(
                            isLoading = false, errorMessage = "لم يتم العثور على بيانات الطالب."
                        )
                    }
                    return@launch
                }

                // 2. جلب معلومات الاختبار (للحصول على العنوان، teacherId و showResultsImmediately)
                val examDoc = firestore.collection("exams").document(examId).get().await()
                val examData = examDoc.data ?: mapOf()

                val examTitle = examData["examTitle"] as? String ?: "اختبار غير معروف"
                // 🔴 استخراج teacherId من وثيقة الاختبار
                val teacherId = examData["teacherId"] as? String
                // 🔴 استخراج شرط إظهار النتيجة فوراً
                val showResultsImmediately = examData["showResultsImmediately"] as? Boolean ?: false


                // 3. 🔴 جلب اسم المادة من كوليكشن 'teachers' باستخدام teacherId
                var subjectName = "مادة غير معروفة"

                if (teacherId != null) {
                    try {
                        // يتم استخدام teacherId لجلب وثيقة المدرس
                        val teacherDoc =
                            firestore.collection("teachers").document(teacherId).get().await()

                        // استخراج حقل 'subject' من وثيقة المدرس
                        val teacherSubject = teacherDoc.data?.get("subject") as? String

                        if (teacherSubject != null) {
                            subjectName = teacherSubject
                        }
                    } catch (e: Exception) {
                        Log.w(
                            "ExamResultViewModel",
                            "Could not fetch subject name for teacherId: $teacherId",
                            e
                        )
                    }
                }

                // 4. جلب بيانات التسليم (Submission)
                val submissionDocId = "${examId}_$studentId"
                val submissionDoc =
                    firestore.collection("exam_submissions").document(submissionDocId).get().await()

                if (!submissionDoc.exists()) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "لم يتم العثور على تسليم لهذا الاختبار."
                        )
                    }
                    return@launch
                }

                val submissionData = submissionDoc.data ?: mapOf()

                val earnedScore = (submissionData["score"] as? Number)?.toString() ?: "0"
                val totalScore = (submissionData["maxScore"] as? Number)?.toString() ?: "?"

                // حالة التقييم: يتم تحديد ما إذا كانت النتيجة مصححة آلياً (isGraded = true)
                val isGraded = submissionData.containsKey("score")

                _state.update {
                    it.copy(
                        isLoading = false,
                        examTitle = examTitle,
                        subjectName = subjectName, // ⬅️ تم تحديثه الآن باسم المادة من بيانات المدرس
                        totalScore = totalScore,
                        earnedScore = earnedScore,
                        isGraded = isGraded,
                        showResultsImmediately = showResultsImmediately
                    )
                }

            } catch (e: Exception) {
                Log.e("ExamResultViewModel", "Error loading exam result: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false, errorMessage = "فشل تحميل النتيجة: ${e.message}"
                    )
                }
            }
        }
    }
}
