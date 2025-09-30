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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.*
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExamScreen(
    onNavigateUp: () -> Unit,
    onExamComplete: () -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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

    ExamScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
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
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppPrimary)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
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
                    Spacer(modifier = Modifier.height(80.dp))

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

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamScreenPreview() {
    SaffiEDUAppTheme {
        val previewState = ExamState(
            examId = "e1",
            examTitle = "اختبار الوحدة الثانية",
            totalQuestions = 10,
            currentQuestionIndex = 6,
            questions = listOf(
                ExamQuestion(
                    id = "q1",
                    text = "هو طلب للمعلومة أو المعرفة أو البيانات، وهو أسلوب يستخدم لجمع المعلومات، أو طلب شيء، أو طلب تركيع أو طلب شيء؟",
                    type = QuestionType.MULTIPLE_CHOICE_SINGLE,
                    points = 1,
                    choices = listOf(
                        Choice(id = "c1", text = "الخيار الأول", isCorrect = true),
                        Choice(id = "c2", text = "الخيار الثاني", isCorrect = false),
                        Choice(id = "c3", text = "الخيار الثالث", isCorrect = false),
                        Choice(id = "c4", text = "الخيار الرابع", isCorrect = false)
                    )
                )
            ),
            userAnswers = mapOf(
                "q1" to ExamAnswer.SingleChoice("c1")
            ),
            remainingTimeInSeconds = 600,
            isLoading = false,
            showTimeWarning = false
        )

        ExamScreenContent(
            state = previewState,
            onEvent = {},
            onNavigateUp = {}
        )
    }
}