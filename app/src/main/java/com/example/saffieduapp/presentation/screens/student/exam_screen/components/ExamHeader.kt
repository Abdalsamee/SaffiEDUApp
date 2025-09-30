package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
 * رأس الاختبار - يحتوي على العنوان + Progress Bar + عدد الأسئلة
 */
@Composable
fun ExamHeader(
    examTitle: String,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = AppPrimary,
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            )
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
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

        // Progress Bar المخصص للاختبار
        val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat()

        ExamProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            height = 8.dp,
            backgroundColor = Color.White.copy(alpha = 0.3f),
            progressBrush = Brush.horizontalGradient(
                listOf(
                    Color.White,
                    AppAlert
                )
            )
        )

        // نص "7 من 10"
        Text(
            text = "${currentQuestionIndex + 1} من $totalQuestions",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamHeaderPreview() {
    SaffiEDUAppTheme {
        ExamHeader(
            examTitle = "اختبار الوحدة الثانية",
            currentQuestionIndex = 9,
            totalQuestions = 10
        )
    }
}