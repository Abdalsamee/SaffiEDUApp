package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.Exam
import com.example.saffieduapp.data.FireBase.ExamRepository
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamState
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizSummaryViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _questions = MutableStateFlow<List<QuestionData>>(emptyList())
    val questions = _questions.asStateFlow()

    fun setQuestions(list: List<QuestionData>) {
        _questions.value = list
    }

    // في ViewModel
    fun deleteQuestion(id: String) {
        _questions.update { it.filterNot { q -> q.id == id } }
    }

    fun publishExam(
        examData: Exam,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestoreData = hashMapOf(
            "className" to examData.className,
            "examTitle" to examData.examTitle,
            "examType" to examData.examType,
            "examDate" to examData.examDate,
            "examStartTime" to examData.examStartTime,
            "examTime" to examData.examTime,
            "randomQuestions" to examData.randomQuestions,
            "showResultsImmediately" to examData.showResultsImmediately,
            "teacherId" to examData.teacherId,
            "teacherName" to examData.teacherName,
            "createdAt" to examData.createdAt,
            "questions" to examData.questions.map { q ->
                hashMapOf(
                    "text" to q.text,
                    "type" to q.type.name,
                    "points" to q.points,
                    "choices" to q.choices.map { choice ->
                        hashMapOf(
                            "text" to choice.text,
                            "isCorrect" to choice.isCorrect
                        )
                    },
                    "essayAnswer" to q.essayAnswer
                )
            }
        )

        firestore.collection("exams")
            .add(firestoreData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "حدث خطأ") }
    }
}