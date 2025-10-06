package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import androidx.lifecycle.ViewModel
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuizSummaryViewModel @Inject constructor() : ViewModel() {
    private val _questions = MutableStateFlow<List<QuestionData>>(emptyList())
    val questions = _questions.asStateFlow()

     fun setQuestions(list: List<QuestionData>) {
        _questions.value = list
    }

    fun deleteQuestion(question: QuestionData) {
        _questions.update { it.filterNot { q -> q == question } }
    }

}
