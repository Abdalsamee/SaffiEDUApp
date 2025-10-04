package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
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
 * Dialog تحذير عدم ظهور الوجه
 */
@Composable
fun NoFaceWarningDialog(
    violationCount: Int,
    remainingWarnings: Int,
    isPaused: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* لا يمكن إغلاقه إلا بالزر */ },
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
                            color = Color(0xFFFF9800).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "تحذير",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                // العنوان
                Text(
                    text = if (isPaused) "⏸️ تم إيقاف الاختبار مؤقتاً" else "⚠️ تحذير",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Cairo,
                    color = Color(0xFFFF9800),
                    textAlign = TextAlign.Center
                )

                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.LightGray
                )

                // الرسالة التوضيحية
                Text(
                    text = if (isPaused) {
                        "لم يتم اكتشاف وجهك لفترة طويلة.\n\nتم إيقاف المؤقت مؤقتاً.\n\nالرجاء الجلوس أمام الكاميرا والضغط على 'متابعة' للاستمرار."
                    } else {
                        "لم يتم اكتشاف وجهك أمام الكاميرا.\n\nالرجاء التأكد من:\n• الجلوس أمام الكاميرا مباشرة\n• وجود إضاءة كافية\n• عدم تغطية الكاميرا"
                    },
                    fontSize = 16.sp,
                    fontFamily = Cairo,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                // عداد التحذيرات
                if (remainingWarnings > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "عدد المرات: $violationCount",
                                fontSize = 15.sp,
                                fontFamily = Cairo,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "التحذيرات المتبقية: $remainingWarnings",
                                fontSize = 14.sp,
                                fontFamily = Cairo,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ آخر تحذير!\nالتكرار التالي سيؤدي لإنهاء الاختبار تلقائياً",
                            fontSize = 14.sp,
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // زر المتابعة
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isPaused) "متابعة الاختبار" else "فهمت",
                        fontSize = 16.sp,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}