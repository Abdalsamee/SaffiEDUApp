package com.example.saffieduapp.presentation.screens.teacher.add_lesson.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp), // ارتفاع افتراضي للزر
        shape = RoundedCornerShape(12.dp), // الحواف المطلوبة
        colors = ButtonDefaults.buttonColors(
            containerColor = AppPrimary // لون التطبيق الأساسي
        )
    ) {
        Text(
            text = text,
            color = Color.White, // لون النص أبيض
            fontSize = 16.sp
        )
    }
}