package com.example.saffieduapp.presentation.screens.teacher.tasks.components

import androidx.compose.foundation.layout.*
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
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherTaskCard(
    type: TaskType, // واجب أو اختبار
    subject: String,
    date: String,
    time: String,
    isActive: Boolean,
    onDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // 🔹 صف علوي: عمودين (معلومات عامة على اليسار، التاريخ والوقت على اليمين)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                // 🔸 العمود الأيسر: العنوان + المادة + الحالة
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${type.label} : الصرف",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1C)
                    )
                    Text(
                        text = subject,
                        fontSize = 15.sp,
                        color = Color(0xFF1C1C1C)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "حالة ${type.statusLabel} : ",
                            fontSize = 14.sp,
                            color = AppTextSecondary
                        )
                        Text(
                            text = if (isActive) "نشط" else "منتهي",
                            fontSize = 14.sp,
                            color = if (isActive) AppPrimary else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 🔸 العمود الأيمن: التاريخ + الوقت
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${type.dateLabel} :",
                        fontSize = 14.sp,
                        color = AppTextSecondary
                    )
                    Text(
                        text = date,
                        fontSize = 14.sp,
                        color = Color(0xFF1C1C1C)
                    )
                    Text(
                        text = "${type.timeLabel} :",
                        fontSize = 14.sp,
                        color = AppTextSecondary
                    )
                    Text(
                        text = time,
                        fontSize = 14.sp,
                        color = Color(0xFF1C1C1C)
                    )
                }
            }


            Spacer(Modifier.height(12.dp))

            // 🔹 أزرار التحكم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = onDetailsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "تفاصيل", color = Color.White, fontSize = 15.sp)
                }
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA24C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "حذف", color = Color.White, fontSize = 15.sp)
                }


            }
        }
    }
}

// 🔹 Enum لتوحيد النصوص حسب النوع
enum class TaskType(
    val label: String,
    val dateLabel: String,
    val timeLabel: String,
    val statusLabel: String
) {
    ASSIGNMENT("الواجب", "تاريخ الانتهاء", "وقت الانتهاء", "المهمة"),
    EXAM("الاختبار", "تاريخ الاختبار", "توقيت الاختبار", "الاختبار")
}


@Preview(showBackground = true, locale = "ar", name = "بطاقة واجب")
@Composable
fun PreviewTeacherTaskCard_Assignment() {
    SaffiEDUAppTheme {
        TeacherTaskCard(
            type = TaskType.ASSIGNMENT,
            subject = "اللغة العربية",
            date = "22/9/2025",
            time = "12:00 am",
            isActive = true,
            onDetailsClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, locale = "ar", name = "بطاقة اختبار")
@Composable
fun PreviewTeacherTaskCard_Exam() {
    SaffiEDUAppTheme {
        TeacherTaskCard(
            type = TaskType.EXAM,
            subject = "الرياضيات",
            date = "22/9/2025",
            time = "12:00 am",
            isActive = false,
            onDetailsClick = {},
            onDeleteClick = {}
        )
    }
}
