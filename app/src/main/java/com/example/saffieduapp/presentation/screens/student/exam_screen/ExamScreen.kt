package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.*
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
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
    Scaffold { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // الهيدر
                ExamHeader(
                    examTitle = state.examTitle,
                    currentQuestionIndex = state.currentQuestionIndex,
                    totalQuestions = state.totalQuestions,
                    remainingTimeInSeconds = state.remainingTimeInSeconds,
                    showTimeWarning = state.showTimeWarning
                )

                // محتوى السؤال
                if (state.questions.isNotEmpty()) {
                    val currentQuestion = state.questions[state.currentQuestionIndex]
                    val currentAnswer = state.userAnswers[currentQuestion.id]

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // بطاقة السؤال
                        QuestionCard(questionText = currentQuestion.text)

                        // الخيارات حسب نوع السؤال
                        when (currentQuestion.type) {
                            QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.TRUE_FALSE -> {
                                currentQuestion.choices.forEach { choice ->
                                    val isSelected = (currentAnswer as? ExamAnswer.SingleChoice)?.choiceId == choice.id
                                    MCQSingleOption(
                                        text = choice.text,
                                        isSelected = isSelected,
                                        onSelect = {
                                            onEvent(ExamEvent.SelectSingleChoice(currentQuestion.id, choice.id))
                                        }
                                    )
                                }
                            }

                            QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                                currentQuestion.choices.forEach { choice ->
                                    val selectedIds = (currentAnswer as? ExamAnswer.MultipleChoice)?.choiceIds ?: emptyList()
                                    val isSelected = choice.id in selectedIds
                                    MCQMultipleOption(
                                        text = choice.text,
                                        isSelected = isSelected,
                                        onToggle = {
                                            onEvent(ExamEvent.ToggleMultipleChoice(currentQuestion.id, choice.id))
                                        }
                                    )
                                }
                            }

                            QuestionType.ESSAY -> {
                                val essayText = (currentAnswer as? ExamAnswer.Essay)?.text ?: ""
                                EssayAnswerField(
                                    value = essayText,
                                    onValueChange = { text ->
                                        onEvent(ExamEvent.UpdateEssayAnswer(currentQuestion.id, text))
                                    }
                                )
                            }
                        }
                    }

                    // أزرار التنقل
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // زر السابق
                        if (state.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = { onEvent(ExamEvent.PreviousQuestion) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "السابق", fontSize = 16.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // زر التالي أو إنهاء
                        if (state.currentQuestionIndex < state.totalQuestions - 1) {
                            AppButton(
                                text = "التالي",
                                onClick = { onEvent(ExamEvent.NextQuestion) },
                                enabled = true,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            AppButton(
                                text = if (state.isSubmitting) "جاري التسليم..." else "إنهاء الاختبار",
                                onClick = { onEvent(ExamEvent.SubmitExam) },
                                enabled = !state.isSubmitting,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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