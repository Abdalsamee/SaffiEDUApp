package com.example.saffieduapp.presentation.screens.student.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressBarWithPercentage(
    progress: Int,
    modifier: Modifier = Modifier,
    progressBarHeight: Dp = 10.dp,
    backgroundColor: Color = Color.White,
    // التدرج عند عدم الاكتمال
    progressBrush: Brush = Brush.horizontalGradient(
        listOf(
            Color.White,
            Color(0xFF43A4D9),
            Color(0xFF0077B6)
        )
    ),
    // اللون الثابت عند 100%
    completedColor: Color = Color(0xFF0077B6),
    textStyle: TextStyle = TextStyle(
        color = Color.Black,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
) {
    val validatedProgress = progress.coerceIn(0, 100)
    val progressFraction = validatedProgress / 100f

    // توحيد نوع الفرشاة (Brush) لتجنّب تعارض الأنواع
    val fillBrush: Brush =
        if (validatedProgress == 100) SolidColor(completedColor) else progressBrush

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // خلفية شريط التقدّم
        Box(
            modifier = Modifier
                .weight(1f)
                .height(progressBarHeight)
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
        ) {
            // جزء التقدّم
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(fillBrush) // الآن دائمًا Brush
            )
        }

        // نسبة التقدّم كنص
        Text(
            text = "$validatedProgress%",
            style = textStyle
        )
    }
}
