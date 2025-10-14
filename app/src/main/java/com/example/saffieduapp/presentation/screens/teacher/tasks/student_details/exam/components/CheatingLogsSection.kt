package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@Composable
fun CheatingLogsSection(
    logs: List<String> // سيتم لاحقًا تمريرها من الـ ViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 🔹 العنوان
        Text(
            text = "محاولات الغش:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppPrimary
        )

        // 🔹 صندوق المحاولات
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE9F2FF), // أزرق فاتح جدًا
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                if (logs.isEmpty()) {
                    // في حال لا توجد محاولات
                    Text(
                        text = "لا توجد محاولات غش مسجلة.",
                        color = AppTextSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    logs.forEach { log ->
                        Text(
                            text = log,
                            color = AppTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun PreviewCheatingLogsSection() {
    CheatingLogsSection(
        logs = listOf(
            "10:05 ص → خرج من التطبيق (تنبيه)",
            "10:15 ص → أوقف الكاميرا",
            "10:20 ص → عودة للامتحان"
        )
    )
}
