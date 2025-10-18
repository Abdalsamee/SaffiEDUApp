package com.example.saffieduapp.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.saffieduapp.R

// ألوان التطبيق
val AppPrimary = Color(0xFF4A90E2)
val CardBackgroundColor = Color(0xFFD2E3F8)
val AppBackground = Color(0xFFFFFFFF)
val AppAlert = Color(0xFFF2994A)
val AppAccent = Color(0xFF6FCF97)
val AppTextPrimary = Color(0xFF333333)
val AppTextSecondary = Color(0xFF828282)

@Composable
fun ChatScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackgroundColor.copy(alpha = 0.2f))
    ) {
        AppDecoration()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // شريط التنبيه
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AppAlert
                    ) {
                        Text(
                            text = "⚠️ تنبيه: ميزة الدردشة غير مُفعّلة",
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppBackground,
                                fontSize = 15.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // الأيقونة والعنوان
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(AppAlert.copy(alpha = 0.15f))
                                .border(3.dp, AppAlert, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.block_24),
                                contentDescription = "ميزة معطلة",
                                tint = AppAlert,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "الميزة معطلة",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = AppTextPrimary
                                )
                            )
                            Text(
                                text = "لم يتم دمجها أبداً",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppAlert
                                )
                            )
                        }
                    }

                    // الملخص
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackgroundColor.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("الميزة غير متوفرة بسبب ")
                                withStyle(SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    color = AppAlert
                                )) {
                                    append("إخفاقات متعددة")
                                }
                                append(" من المطور المسؤول")
                            },
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = AppTextPrimary,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // الإخفاقات الثلاثة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactFailureItem(
                            modifier = Modifier.weight(1f),
                            icon = "⏰",
                            title = "تأخير شديد",
                            badge = "حرج"
                        )
                        CompactFailureItem(
                            modifier = Modifier.weight(1f),
                            icon = "🤷‍♂️",
                            title = "عدم فهم تقني",
                            badge = "خطير"
                        )
                        CompactFailureItem(
                            modifier = Modifier.weight(1f),
                            icon = "📉",
                            title = "سوء إدارة",
                            badge = "حرج"
                        )
                    }

                    // التقييم
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackgroundColor.copy(alpha = 0.5f))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricColumn("الإنجاز", "0%")
                        VerticalDivider(
                            modifier = Modifier.height(35.dp),
                            thickness = 2.dp,
                            color = AppPrimary.copy(alpha = 0.3f)
                        )
                        MetricColumn("الالتزام", "فاشل")
                        VerticalDivider(
                            modifier = Modifier.height(35.dp),
                            thickness = 2.dp,
                            color = AppPrimary.copy(alpha = 0.3f)
                        )
                        MetricColumn("التقييم", "⭐ 1/5")
                    }

                    // الخلاصة
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = AppPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("الخلاصة: ")
                                }
                                append("غياب المسؤولية وضعف القدرات التقنية وسوء التخطيط أدى لتعطيل الميزة")
                            },
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = AppTextPrimary,
                                lineHeight = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactFailureItem(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    badge: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppBackground)
            .border(2.dp, AppPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = AppTextPrimary
            ),
            textAlign = TextAlign.Center
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = AppAlert
        ) {
            Text(
                text = badge,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppBackground,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                color = AppTextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AppAlert
            )
        )
    }
}

@Composable
fun AppDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-70).dp, y = (-70).dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(AppPrimary.copy(alpha = 0.05f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .size(190.dp)
                .clip(CircleShape)
                .background(AppPrimary.copy(alpha = 0.08f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 110.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(AppPrimary.copy(alpha = 0.06f))
        )
    }
}