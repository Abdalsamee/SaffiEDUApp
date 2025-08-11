package com.example.saffieduapp.presentation.screens.student.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.Cairo

@Composable
fun CommonTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null // جعلناه اختياريًا (nullable)
) {
    // ١. استخدام Box للحصول على الشكل المطلوب مع الحواف الدائرية
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp) // يمكنك تعديل الارتفاع حسب الحاجة
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(AppPrimary),
        contentAlignment = Alignment.BottomCenter // لتوسيط العنوان تلقائيًا
    ) {
        // ٢. زر الرجوع يظهر فقط إذا تم تمرير دالة onNavigateUp
        if (onNavigateUp != null) {
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier.align(Alignment.BottomStart) // أسفل يسار
                    .padding(start = 8.dp, bottom = 18.dp) // محاذاة لليسار
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }


        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Cairo,
            modifier = Modifier.padding(bottom = 18.dp)
        )
    }
}