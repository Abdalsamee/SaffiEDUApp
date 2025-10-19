package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.*
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExamScreen(
    onNavigateUp: () -> Unit,
    onExamComplete: () -> Unit,
    examId: String,
    viewModel: ExamViewModel = hiltViewModel(),

    ) {

    // عندما يتغير الـ examId أو عند أول دخول، حمّل البيانات
    LaunchedEffect(examId) {
        if (examId.isNotBlank()) {
            viewModel.loadExam(examId)
        }
    }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSubmitDialog by remember { mutableStateOf(false) }

    // معالجة الأحداث
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ExamUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is ExamUiEvent.ExamCompleted -> {
                    Toast.makeText(context, "تم تسليم الاختبار بنجاح", Toast.LENGTH_SHORT).show()
                    onExamComplete()
                }

                is ExamUiEvent.TimeExpired -> {
                    Toast.makeText(context, "انتهى وقت الاختبار", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showSubmitDialog) {
        ExamSubmitDialog(
            remainingTimeInSeconds = state.remainingTimeInSeconds,
            onDismiss = { showSubmitDialog = false },
            onConfirm = {
                showSubmitDialog = false
                viewModel.onEvent(ExamEvent.SubmitExam)
            }
        )
    }

    ExamScreenContent(
        state = state,
        onEvent = { event ->
            if (event is ExamEvent.SubmitExam) {
                showSubmitDialog = true
            } else {
                viewModel.onEvent(event)
            }
        },
        onNavigateUp = onNavigateUp
    )
}

@Composable
private fun ExamScreenContent(
    state: ExamState,
    onEvent: (ExamEvent) -> Unit,
    onNavigateUp: () -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppPrimary)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // الهيدر الأزرق
            ExamHeader(
                examTitle = state.examTitle,
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.totalQuestions
            )

            // الكارد الأبيض المرتفع فوق الهيدر
            if (state.questions.isNotEmpty()) {
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val currentAnswer = state.userAnswers[currentQuestion.id]
                val isLastQuestion = state.currentQuestionIndex == state.totalQuestions - 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // مساحة للهيدر
                    Spacer(modifier = Modifier.height(150.dp))

                    // الكارد الأبيض
                    ExamQuestionBox(
                        question = currentQuestion,
                        currentQuestionIndex = state.currentQuestionIndex,
                        remainingTimeInSeconds = state.remainingTimeInSeconds,
                        showTimeWarning = state.showTimeWarning,
                        currentAnswer = currentAnswer,
                        onEvent = onEvent,
                        isLastQuestion = isLastQuestion,
                        isSubmitting = state.isSubmitting
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}