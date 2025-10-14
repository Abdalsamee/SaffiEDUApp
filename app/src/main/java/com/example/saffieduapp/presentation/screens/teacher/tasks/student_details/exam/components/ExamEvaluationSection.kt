package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.CardBackgroundColor

@Composable
fun ExamEvaluationSection(
    earnedScore: String,
    totalScore: String,
    onScoreChange: (String) -> Unit,
    answerStatus: String,
    totalTime: String,
    examStatus: String,
    onViewAnswersClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 🔹 الدرجة المستحقة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الدرجة المستحقة:", fontSize = 15.sp, color = Color.Black)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = earnedScore,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        onScoreChange(filtered)
                    },
                    modifier = Modifier
                        .width(70.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "من $totalScore",
                    color = AppTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 🔹 حالة الإجابات
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("حالة الإجابات:", fontSize = 15.sp, color = Color.Black)

            Button(
                onClick = onViewAnswersClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(160.dp)
            ) {
                Text("مشاهدة الإجابات", fontSize = 11.sp, color = Color.White)
            }
        }

        // 🔹 الوقت الكلي للمحاولة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الوقت الكلي للمحاولة:", fontSize = 15.sp, color = Color.Black)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardBackgroundColor,
                modifier = Modifier.width(160.dp)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(totalTime, fontSize = 14.sp)
                }
            }
        }

        // 🔹 الحالة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الحالة:", fontSize = 15.sp, color = Color.Black)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardBackgroundColor,
                modifier = Modifier.width(160.dp)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(examStatus, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun PreviewExamEvaluationSection() {
    ExamEvaluationSection(
        earnedScore = "15",
        totalScore = "20",
        onScoreChange = {},
        answerStatus = "مكتملة",
        totalTime = "45 دقيقة",
        examStatus = "مستبعد",
        onViewAnswersClick = {}
    )
}
