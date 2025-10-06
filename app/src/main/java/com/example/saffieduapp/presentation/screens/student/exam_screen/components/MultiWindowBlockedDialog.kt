package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
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
 * Dialog يظهر عند محاولة فتح الاختبار في وضع Multi-Window
 */
@Composable
fun MultiWindowBlockedDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
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
                // أيقونة المنع
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
                        imageVector = Icons.Default.Block,
                        contentDescription = "ممنوع",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                // العنوان
                Text(
                    text = "⚠️ وضع تقسيم الشاشة غير مسموح",
                    fontSize = 20.sp,
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
                    text = "لا يمكن بدء الاختبار في وضع تقسيم الشاشة (Split Screen)",
                    fontSize = 16.sp,
                    fontFamily = Cairo,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // الخطوات المطلوبة
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "الخطوات المطلوبة:",
                            fontSize = 15.sp,
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )

                        Text(
                            text = "1. أغلق وضع تقسيم الشاشة",
                            fontSize = 14.sp,
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "2. افتح التطبيق في وضع الشاشة الكاملة",
                            fontSize = 14.sp,
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "3. حاول بدء الاختبار مرة أخرى",
                            fontSize = 14.sp,
                            fontFamily = Cairo,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // زر الإغلاق
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
                        text = "حسناً، فهمت",
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