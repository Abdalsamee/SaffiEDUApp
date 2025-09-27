package com.example.saffieduapp.presentation.screens.student.exam_details.components

import androidx.compose.foundation.Canvas
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
import com.example.saffieduapp.ui.theme.Cairo
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme


@Composable
fun ExamInstructionsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("أوافق", color = Color.White)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC9595)) // أحمر
                ) {
                    Text("لا، أوافق",color = Color.White)
                }

            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83D\uDEA8تعليمات الاختبار",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "يرجى قراءة التعليمات التالية بعناية قبل الموافقة",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InstructionItem(text = "سيتم تشغيل الكاميرا والتقاط صور تلقائية أثناء مدة الاختبار.")
                InstructionItem(text = "يُرجى من جميع الطالبات الالتزام باللباس المحتشم المناسب لتفعيل الكاميرا.")
                InstructionItem(text = "الخروج من التطبيق أو تغيير الشاشة غير مسموح:\n•الخروج الأول: تنبيه تلقائي.\n•الخروج الثاني: استبعاد مباشر من الاختبار. ")
                InstructionItem(text = "تأكد/ي من وجود اتصال إنترنت مستقر وكاميرا فعالة قبل البدء في الاختبار.")
                InstructionItem(text = "يخضع هذا الاختبار لرقابة تقنية دقيقة لضمان بيئة عادلة وآمنة لجميع المشاركين.")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White.copy(alpha = 0.9f)


    )
}

@Composable
private fun InstructionItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(8.dp)
        ) {
            drawCircle(color = Color.Black)
        }

        Text(text = text, fontSize = 14.sp, color = Color.Black, fontFamily = Cairo, fontWeight = FontWeight.SemiBold)
    }
}
@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamInstructionsDialogPreview() {
    SaffiEDUAppTheme {
        ExamInstructionsDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}