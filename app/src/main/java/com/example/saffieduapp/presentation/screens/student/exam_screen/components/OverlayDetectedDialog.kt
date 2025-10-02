package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.saffieduapp.ui.theme.Cairo

/**
 * Dialog تحذيري عند اكتشاف Overlay
 * هذا الـ Dialog لا يمكن إغلاقه - سيتم إنهاء الاختبار تلقائياً
 */
@Composable
fun OverlayDetectedDialog(
    violationType: String,
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { /* لا يمكن إغلاقه */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // أيقونة التحذير
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFFF5252).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تحذير",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF5252)
                    )
                }

                // العنوان
                Text(
                    text = "⚠️ تم اكتشاف مخالفة أمنية",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Cairo,
                    color = Color(0xFFFF5252),
                    textAlign = TextAlign.Center
                )

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                // الرسالة التوضيحية
                val message = when (violationType) {
                    "OVERLAY_DETECTED" -> "تم اكتشاف نافذة منبثقة فوق شاشة الاختبار.\n\nقد تكون:\n• تطبيقات Chat Heads\n• Screen Filter\n• Accessibility Tools\n• Any Overlay App"
                    "PIP_MODE_DETECTED" -> "تم اكتشاف محاولة استخدام وضع Picture-in-Picture"
                    "MULTI_WINDOW_DETECTED" -> "تم اكتشاف استخدام وضع النوافذ المتعددة (Split Screen)"
                    "EXTERNAL_DISPLAY_CONNECTED" -> "تم اكتشاف توصيل شاشة خارجية"
                    else -> "تم اكتشاف نشاط مشبوه"
                }

                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontFamily = Cairo,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // رسالة الإنهاء
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFF5252).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "سيتم إنهاء الاختبار تلقائياً وإبلاغ المعلم",
                        fontSize = 15.sp,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // زر الإغلاق (سيقوم بإنهاء الاختبار)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "إنهاء الاختبار",
                        fontSize = 16.sp,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}