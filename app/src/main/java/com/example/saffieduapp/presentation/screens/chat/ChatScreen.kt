package com.example.saffieduapp.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppBackground
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextPrimary

@Composable
fun ChatScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(24.dp)
        ) {

            // أيقونة كبيرة وواضحة
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AppPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chat),
                    contentDescription = "Chat",
                    tint = AppPrimary,
                    modifier = Modifier.size(50.dp)
                )
            }

            // العنوان
            Text(
                text = "ميزة الدردشة غير متاحة حالياً",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = AppTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            // النص المختصر
            Text(
                text = "يتم حالياً إعادة تصميم نظام الدردشة لضمان أداء آمن ومستقر. ستتوفر الميزة في تحديثات لاحقة.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AppTextPrimary.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )

            // ملاحظة حول الزر
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = AppAlert.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "زر الدردشة سيبقى ضمن التطبيق كعنصر أساسي، وسيتم تفعيله لاحقاً.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AppTextPrimary,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // زر الرجوع
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(180.dp)
                    .height(45.dp)
            ) {
                Text(
                    text = "عودة",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = AppBackground,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
