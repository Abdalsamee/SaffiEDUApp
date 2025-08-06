package com.example.saffieduapp.presentation.screens.student.component



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppSecondary

@Composable
fun ProgressBarWithPercentage(
    progress: Int,
    modifier: Modifier = Modifier,
    progressBarHeight: Dp = 10.dp,
    backgroundColor: Color = Color.White,
    progressBrush: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color.White,
            Color(0xFF43A4D9), // لون بداية متدرج أغمق قليلاً
            Color(0xFF0077B6)  // اللون الأصلي
        )
    ),
    textStyle: TextStyle = TextStyle(
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
) {
    // التأكد من أن قيمة التقدم بين 0 و 100
    val validatedProgress = progress.coerceIn(0, 100)
    val progressFraction = validatedProgress / 100f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp) // مسافة بين الشريط والنص
    ) {
        // حاوية شريط التقدم
        Box(
            modifier = Modifier
                .weight(1f) // ليأخذ المساحة المتبقية
                .height(progressBarHeight)
                .clip(RoundedCornerShape(50)) // حواف دائرية بالكامل
                .background(backgroundColor)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(progressBrush)
            )
        }

        // نص النسبة المئوية
        Text(
            text = "$validatedProgress%", // أضفت علامة % لتكون أقرب للنص
            style = textStyle,
        )
    }
}
