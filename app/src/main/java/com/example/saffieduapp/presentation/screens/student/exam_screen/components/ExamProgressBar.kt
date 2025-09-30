package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * خط تقدم بسيط للاختبار
 * يدعم التدرج اللوني ولا يحتوي على نص النسبة المئوية
 */
@Composable
fun ExamProgressBar(
    progress: Float, // من 0.0 إلى 1.0
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.3f),
    progressBrush: Brush = Brush.horizontalGradient(
        listOf(
            Color.White,
            Color(0xffF3A25A),
            AppAlert
        )

    ),completedColor: Color = AppAlert

) {
    // التأكد من أن القيمة بين 0 و 1
    val validatedProgress = progress.coerceIn(0f, 1f)
    val fillBrush = if (validatedProgress >= 1f) {
        // عند 100% - برتقالي بالكامل
        Brush.horizontalGradient(listOf(completedColor, completedColor))
    } else {
        // قبل 100% - تدرج عادي
        progressBrush
    }
    // خلفية شريط التقدم
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
    ) {
        // جزء التقدم
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(validatedProgress)
                .clip(RoundedCornerShape(50))
                .background(fillBrush)
        )
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamProgressBarPreview() {
    SaffiEDUAppTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppPrimary)
                .padding(20.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            // مثال 1: تقدم 0%
            ExamProgressBar(
                progress = 0f
            )

            // مثال 2: تقدم 30%
            ExamProgressBar(
                progress = 0.3f
            )

            // مثال 3: تقدم 70%
            ExamProgressBar(
                progress = 0.7f
            )

            // مثال 4: تقدم 100%
            ExamProgressBar(
                progress = 1f
            )

            // مثال 5: مع تدرج مختلف
            ExamProgressBar(
                progress = 0.5f,
                progressBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFFD93D)
                    )
                )
            )
        }
    }
}