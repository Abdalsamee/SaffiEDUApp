package com.example.saffieduapp.presentation.screens.teacher.add_question.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_question.AddQuestionEvent
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun QuestionChoicesSection(
    questionType: QuestionType,
    choices: List<Choice>,
    essayAnswer: String,
    onEvent: (AddQuestionEvent) -> Unit
) {
    var showInstructionsDialog by remember { mutableStateOf(false) }

    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),

            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(AppPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Instructions Icon",
                        tint = AppPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },

            title = {
                Text(
                    text = "تعليمات إضافة الخيارات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    InstructionItem("حدد الإجابة الصحيحة بالنقر على الدائرة أو المربع بجانب الخيار.")
                    InstructionItem("يمكنك إضافة حتى 5 خيارات كحد أقصى.")
                    InstructionItem("يمكنك حذف أي خيار بالنقر على أيقونة الحذف.")
                    InstructionItem("تأكد من تحديد إجابة صحيحة واحدة على الأقل.")
                }
            },

            confirmButton = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showInstructionsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "حسناً، فهمت",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        )
    }

    // باقي الكود يبقى كما هو دون تغيير
    when (questionType) {
        QuestionType.MULTIPLE_CHOICE_SINGLE,
        QuestionType.MULTIPLE_CHOICE_MULTIPLE,
        QuestionType.TRUE_FALSE -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // تقليل المسافة قليلاً
                ){
                    Text(
                        text = "إضافة خيارات:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.qustiondialog),
                        contentDescription = "Show Instructions",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp) // تحديد حجم الأيقونة
                            .clickable { showInstructionsDialog = true }
                    )
                }

                choices.forEach { choice ->
                    key(choice.id) {
                        ChoiceInputField(
                            choice = choice,
                            questionType = questionType,
                            onEvent = onEvent,
                            canBeDeleted = choices.size > 2
                        )
                    }
                }

                if (questionType != QuestionType.TRUE_FALSE && choices.size < 5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .height(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 20.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 20.dp
                                )
                            )
                            .background(Color.White, shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 20.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 20.dp
                            ))
                            .clickable { onEvent(AddQuestionEvent.AddChoiceClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.addbtnn),
                                contentDescription = "Add Choice",
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إضافة خيار",
                                fontSize = 14.sp,
                                color = AppPrimary
                            )
                        }
                    }
                }
            }
        }

        QuestionType.ESSAY -> {
            AddLessonTextField(
                title = "إضافة الإجابة النموذجية",
                value = essayAnswer,
                onValueChange = {onEvent(AddQuestionEvent.EssayAnswerChanged(it))} ,
                placeholder = "ادخل الإجابة النموذجية للسؤال المقالي...",
                modifier = Modifier.height(150.dp)
            )
        }
    }
}


@Composable
fun InstructionItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Icon(
            painter = painterResource(id = R.drawable.qustiondialog),
            contentDescription = null,
            tint =Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            fontSize = 17.sp
        )
    }
}