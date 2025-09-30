package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

/**
 * Dialog تحذير عند العودة للتطبيق بعد الخروج
 */
@Composable
fun ExamReturnWarningDialog(
    exitAttempts: Int,
    remainingAttempts: Int,
    onContinue: () -> Unit
) {
    val isLastWarning = remainingAttempts == 0

    Dialog(
        onDismissRequest = { /* لا يمكن الإغلاق */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // أيقونة تحذير
                Text(
                    text = if (isLastWarning) "🚨" else "⚠️",
                    fontSize = 64.sp
                )

                // عنوان التحذير
                Text(
                    text = if (isLastWarning) "تحذير نهائي!" else "تم تسجيل خروجك",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLastWarning) Color(0xFFFF4444) else AppAlert
                )

                // رقم المحاولة
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLastWarning) Color(0xFFFFEBEE) else AppAlert.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "المحاولة رقم: $exitAttempts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLastWarning) Color(0xFFFF4444) else AppAlert,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }

                // نص التحذير
                Text(
                    text = if (isLastWarning) {
                        "تم تسجيل خروجك من التطبيق!\n\n" +
                                "⚠️ هذا آخر تحذير!\n\n" +
                                "إذا خرجت مرة أخرى سيتم إنهاء الاختبار تلقائياً وإرسال تقرير أمني للمعلم."
                    } else {
                        "تم تسجيل خروجك من التطبيق!\n\n" +
                                "المحاولات المتبقية: $remainingAttempts\n\n" +
                                "سيتم إنهاء الاختبار تلقائياً إذا خرجت مرة أخرى."
                    },
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // زر المتابعة
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastWarning) Color(0xFFFF4444) else AppPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "فهمت، المتابعة",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamReturnWarningDialogPreview() {
    SaffiEDUAppTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // التحذير الأول
            ExamReturnWarningDialog(
                exitAttempts = 1,
                remainingAttempts = 1,
                onContinue = {}
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamReturnWarningDialogLastPreview() {
    SaffiEDUAppTheme {
        // التحذير النهائي
        ExamReturnWarningDialog(
            exitAttempts = 2,
            remainingAttempts = 0,
            onContinue = {}
        )
    }
}