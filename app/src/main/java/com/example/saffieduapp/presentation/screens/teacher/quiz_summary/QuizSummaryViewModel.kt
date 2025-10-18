package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.ChoiceDto
import com.example.saffieduapp.data.FireBase.Exam
import com.example.saffieduapp.data.FireBase.ExamDto
import com.example.saffieduapp.data.FireBase.ExamRepository
import com.example.saffieduapp.data.FireBase.QuestionDto
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
    private val repository: ExamRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionData>>(emptyList())
    val questions = _questions.asStateFlow()

    fun setQuestions(list: List<QuestionData>) {
        _questions.value = list
    }

    fun deleteQuestion(id: String) {
        _questions.update { it.filterNot { q -> q.id == id } }
    }

    fun publishExam(
        examState: AddExamState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val qList = _questions.value.map { q ->
            QuestionDto(
                id = q.id,
                text = q.text,
                type = q.type.name, // store enum name
                points = q.points.toIntOrNull() ?: 0,
                choices = q.choices.map { c ->
                    ChoiceDto(id = c.id, text = c.text, isCorrect = c.isCorrect)
                },
                essayAnswer = q.essayAnswer
            )
        }

        val examDto = ExamDto(
            className = examState.selectedClass,
            examTitle = examState.examTitle,
            examType = examState.examType,
            examDate = examState.examDate,
            examStartTime = examState.examStartTime,
            examTime = examState.examTime,
            randomQuestions = examState.randomQuestions,
            showResultsImmediately = examState.showResultsImmediately,
            teacherId = examState.teacherId,
            teacherName = examState.teacherName,
            // createdAt left null -> repo will use serverTimestamp
            questions = qList
        )

        viewModelScope.launch {
            val (success, err) = repository.addExamWithQuestions(examDto)
            if (success) onSuccess() else onError(err ?: "فشل في نشر الاختبار")
        }
    }
}