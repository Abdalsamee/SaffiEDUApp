package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * رأس الاختبار - يحتوي على:
 * - عنوان الاختبار
 * - Progress Bar
 * - رقم السؤال الحالي
 * - المؤقت
 */
@Composable
fun ExamHeader(
    examTitle: String,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    remainingTimeInSeconds: Int,
    showTimeWarning: Boolean, // عرض المؤقت بالأحمر
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppPrimary,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // عنوان الاختبار
        Text(
            text = examTitle,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AppAlert,
                trackColor = Color.Transparent
            )
        }

        // الصف السفلي: المؤقت + رقم السؤال
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // المؤقت
            Box(
                modifier = Modifier
                    .background(
                        color = if (showTimeWarning) Color(0xFFFF4444) else Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = formatTime(remainingTimeInSeconds),
                    color = if (showTimeWarning) Color.White else AppPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // رقم السؤال
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = AppAlert, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentQuestionIndex + 1}\nس",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        // نص "7 من 10" أسفل الـ Progress
        Text(
            text = "${currentQuestionIndex + 1} من $totalQuestions",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
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
private fun ExamHeaderPreview() {
    SaffiEDUAppTheme {
        Column {
            ExamHeader(
                examTitle = "اختبار الوحدة الثانية",
                currentQuestionIndex = 6,
                totalQuestions = 10,
                remainingTimeInSeconds = 600, // 10 دقائق
                showTimeWarning = false
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExamHeader(
                examTitle = "اختبار الوحدة الثانية",
                currentQuestionIndex = 6,
                totalQuestions = 10,
                remainingTimeInSeconds = 59, // آخر دقيقة
                showTimeWarning = true
            )
        }
    }
}