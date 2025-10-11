package com.example.saffieduapp.presentation.screens.student.exam_details

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

    private fun loadExamDetails(examId: String) {
        viewModelScope.launch {
            try {
                _state.value = ExamDetailsState(isLoading = true)

                // جلب بيانات الاختبار من مجموعة exams في Firestore
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

    private fun mapToExamDetails(id: String, data: Map<String, Any>): ExamDetails {
        // تحويل البيانات من Firestore إلى نموذج ExamDetails
        return ExamDetails(
            id = id,
            title = data["examTitle"] as? String ?: "بدون عنوان",
            subjectName = data["className"] as? String ?: "بدون مادة",
            teacherName = data["teacherName"] as? String ?: "بدون معلم",
            imageUrl = "", // يمكنك إضافة صورة المادة إذا كانت متوفرة
            date = data["examDate"] as? String ?: "غير محدد",
            startTime = data["examStartTime"] as? String ?: "غير محدد",
            endTime = calculateEndTime(
                data["examStartTime"] as? String,
                data["examTime"] as? String
            ),
            durationInMinutes = (data["examTime"] as? String)?.toIntOrNull() ?: 0,
            questionCount = getQuestionCount(data["questions"]),
            status = determineExamStatus(
                data["examDate"] as? String,
                data["examStartTime"] as? String
            ),
        )
    }

    private fun calculateEndTime(startTime: String?, duration: String?): String {
        if (startTime == null) return "غير محدد"

        // تحديد وقت الانتهاء ليكون دائمًا 11:59 مساءً
        return "11:59 مساءً"
    }

    private fun getQuestionCount(questions: Any?): Int {
        return when (questions) {
            is List<*> -> questions.size
            else -> 0
        }
    }

    private fun determineExamStatus(examDate: String?, examStartTime: String?): String {
        // منطق تحديد حالة الاختبار (متاح، منتهي، لم يبدأ بعد)
        // يمكنك تحسين هذا المنطق حسب احتياجاتك
        return "متاح"
    }
}