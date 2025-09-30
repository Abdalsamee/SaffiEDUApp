package com.example.saffieduapp.presentation.screens.teacher.components

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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = { if (enabled) onClick() }, // منع التنفيذ عند التعطيل
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled, // ← هنا نربطه مع Compose
        colors = ButtonDefaults.buttonColors(
            containerColor = AppPrimary,
            disabledContainerColor = Color.Gray,  // لون الخلفية عند التعطيل
            contentColor = Color.White,
            disabledContentColor = Color.LightGray // لون النص عند التعطيل
        )
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}


