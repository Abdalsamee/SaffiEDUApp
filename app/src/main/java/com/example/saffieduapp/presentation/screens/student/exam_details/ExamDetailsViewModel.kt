package com.example.saffieduapp.presentation.screens.student.exam_details


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
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
class ExamDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ExamDetailsState())
    val state = _state.asStateFlow()

    init {
        val examId = savedStateHandle.get<String>("examId")
        if (examId != null) {
            loadExamDetails(examId)
        } else {
            _state.value = ExamDetailsState(
                isLoading = false,
                error = "لم يتم العثور على معرف الاختبار"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExamDetails(examId: String) {
        viewModelScope.launch {
            try {
                _state.value = ExamDetailsState(isLoading = true)

                // جلب بيانات الاختبار من Firestore
                val document = firestore.collection("exams")
                    .document(examId)
                    .get()
                    .await()

                if (document.exists()) {
                    val examDetails = mapToExamDetails(document.id, document.data!!)
                    _state.value = ExamDetailsState(
                        isLoading = false,
                        examDetails = examDetails
                    )
                } else {
                    _state.value = ExamDetailsState(
                        isLoading = false,
                        error = "لم يتم العثور على الاختبار"
                    )
                }
            } catch (e: Exception) {
                _state.value = ExamDetailsState(
                    isLoading = false,
                    error = "حدث خطأ في جلب البيانات: ${e.message}"
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapToExamDetails(id: String, data: Map<String, Any>): ExamDetails {
        return ExamDetails(
            id = id,
            title = data["examTitle"] as? String ?: "بدون عنوان",
            subjectName = data["className"] as? String ?: "بدون مادة",
            teacherName = data["teacherName"] as? String ?: "بدون معلم",
            imageUrl = "", // يمكن إضافة صورة لاحقًا
            date = data["examDate"] as? String ?: "غير محدد",
            startTime = data["examStartTime"] as? String ?: "غير محدد",
            endTime = calculateEndTime(data["examDate"] as? String),
            durationInMinutes = (data["examTime"] as? String)?.toIntOrNull() ?: 0,
            questionCount = getQuestionCount(data["questions"]),
            status = determineExamStatus(
                data["examDate"] as? String,
                data["examStartTime"] as? String
            ),
        )
    }

    /**
     * تحديد وقت الانتهاء ليكون في نفس يوم الاختبار الساعة 23:59 مساءً.
     */
    private fun calculateEndTime(examDate: String?): String {
        return if (examDate.isNullOrBlank()) {
            "غير محدد"
        } else {
            "$examDate 23:59 مساءً"
        }
    }

    private fun getQuestionCount(questions: Any?): Int {
        return when (questions) {
            is List<*> -> questions.size
            else -> 0
        }
    }

    /**
     * تحديد حالة الاختبار بناءً على التاريخ ووقت البداية.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun determineExamStatus(examDate: String?, examStartTime: String?): String {
        if (examDate.isNullOrBlank() || examStartTime.isNullOrBlank()) return "غير محدد"

        return try {
            val dateTimeString = "$examDate $examStartTime" // مثال: 2025-10-08 14:22
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val examDateTime = java.time.LocalDateTime.parse(dateTimeString, formatter)
            val now = java.time.LocalDateTime.now()

            when {
                now.isBefore(examDateTime) -> "لم يبدأ بعد"
                else -> "متاح"
            }
        } catch (e: Exception) {
            "غير محدد"
        }
    }
}