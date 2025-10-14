package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers.StudentAnswer
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun AnswerItemCard(
    answer: StudentAnswer,
    onScoreSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(answer.questionText, fontSize = 15.sp, fontWeight = FontWeight.Medium)

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFD8E8FF),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(
                text = answer.answerText,
                modifier = Modifier.padding(10.dp),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 🔹 القائمة المنسدلة للدرجة
        Box {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(90.dp).height(40.dp)
            ) {
                Text(
                    text = answer.assignedScore?.toString() ?: "الدرجة",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (i in 0..answer.maxScore) {
                    DropdownMenuItem(
                        text = { Text(i.toString()) },
                        onClick = {
                            expanded = false
                            onScoreSelected(i)
                        }
                    )
                }
            }
        }
    }
}
