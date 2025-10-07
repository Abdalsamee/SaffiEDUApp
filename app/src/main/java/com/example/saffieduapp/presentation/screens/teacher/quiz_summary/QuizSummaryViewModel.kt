package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuizSummaryViewModel @Inject constructor(
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

    fun saveExam(examTitle: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val examData = hashMapOf(
            "title" to examTitle,
            "questions" to _questions.value.map { question ->
                hashMapOf(
                    "text" to question.text,
                    "type" to question.type.name,
                    "points" to question.points,
                    "choices" to question.choices.map { choice ->
                        hashMapOf(
                            "text" to choice.text,
                            "isCorrect" to choice.isCorrect
                        )
                    },
                    "essayAnswer" to question.essayAnswer
                )
            }
        )

        firestore.collection("exams")
            .add(examData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "حدث خطأ") }
    }
}
