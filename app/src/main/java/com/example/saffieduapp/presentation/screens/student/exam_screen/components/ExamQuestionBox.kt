package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.presentation.screens.student.exam_screen.ExamAnswer
import com.example.saffieduapp.presentation.screens.student.exam_screen.ExamEvent
import com.example.saffieduapp.presentation.screens.student.exam_screen.ExamQuestion
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * الكارد الأبيض الذي يحتوي على السؤال والخيارات
 * يرتفع فوق الهيدر
 */
@Composable
fun ExamQuestionBox(
    question: ExamQuestion,
    currentQuestionIndex: Int,
    remainingTimeInSeconds: Int,
    showTimeWarning: Boolean,
    currentAnswer: ExamAnswer?,
    onEvent: (ExamEvent) -> Unit,
    isLastQuestion: Boolean,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        tonalElevation = 0.dp
    ) {
        // <-- الخطوة 1: استخدام Box كحاوية رئيسية للسماح بالتكديس والمحاذاة
        Box(modifier = Modifier.fillMaxWidth()) {
            // المحتوى الرئيسي مع padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp), // هذا الـ padding يطبق على كل المحتوى داخل الـ Column
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // الصف العلوي: يحتوي الآن على المؤقت فقط
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // <-- الخطوة 4: تغيير المحاذاة لأن رقم السؤال لم يعد هنا
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // المؤقت (يسار)
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (showTimeWarning) Color(0xFFFF4444) else AppAlert,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = formatTime(remainingTimeInSeconds),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // نص السؤال
                Text(
                    text = question.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    lineHeight = 28.sp
                )

                // الخيارات حسب نوع السؤال
                when (question.type) {
                    QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.TRUE_FALSE -> {
                        question.choices.forEach { choice ->
                            val isSelected = (currentAnswer as? ExamAnswer.SingleChoice)?.choiceId == choice.id
                            MCQSingleOption(
                                text = choice.text,
                                isSelected = isSelected,
                                onSelect = {
                                    onEvent(ExamEvent.SelectSingleChoice(question.id, choice.id))
                                }
                            )
                        }
                    }

                    QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                        question.choices.forEach { choice ->
                            val selectedIds = (currentAnswer as? ExamAnswer.MultipleChoice)?.choiceIds ?: emptyList()
                            val isSelected = choice.id in selectedIds
                            MCQMultipleOption(
                                text = choice.text,
                                isSelected = isSelected,
                                onToggle = {
                                    onEvent(ExamEvent.ToggleMultipleChoice(question.id, choice.id))
                                }
                            )
                        }
                    }

                    QuestionType.ESSAY -> {
                        val essayText = (currentAnswer as? ExamAnswer.Essay)?.text ?: ""
                        EssayAnswerField(
                            value = essayText,
                            onValueChange = { text ->
                                onEvent(ExamEvent.UpdateEssayAnswer(question.id, text))
                            }
                        )
                    }
                }

                // زر التالي أو إنهاء
                if (isLastQuestion) {
                    AppButton(
                        text = if (isSubmitting) "جاري التسليم..." else "إنهاء الاختبار",
                        onClick = { onEvent(ExamEvent.SubmitExam) },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    AppButton(
                        text = "التالي",
                        onClick = { onEvent(ExamEvent.NextQuestion) },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(0.4f)
                    )
                }
            }

            // <-- الخطوة 2 و 3: وضع صندوق رقم السؤال هنا بمحاذاة الحافة العلوية
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart) // <-- هذا هو السطر الأهم، يجعله يلتصق بالزاوية العلوية اليمنى
                    .size(width = 60.dp, height = 50.dp) // يمكنك تعديل الارتفاع حسب رغبتك
                    .padding(start = 16.dp)
                    .background(
                        color = AppAlert, shape = RoundedCornerShape(
                            bottomEnd = 12.dp,
                            bottomStart = 12.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentQuestionIndex + 1}\nس",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
/**
 * تنسيق الوقت بصيغة 00:10:00
 */
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamQuestionBoxPreview() {
    SaffiEDUAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            ExamQuestionBox(
                question = ExamQuestion(
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
                ),
                currentQuestionIndex = 6,
                remainingTimeInSeconds = 600,
                showTimeWarning = false,
                currentAnswer = ExamAnswer.SingleChoice("c1"),
                onEvent = {},
                isLastQuestion = false,
                isSubmitting = false,
                modifier = Modifier.padding(top = 80.dp)
            )
        }
    }
}