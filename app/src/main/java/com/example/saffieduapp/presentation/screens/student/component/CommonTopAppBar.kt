package com.example.saffieduapp.presentation.screens.student.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
    onNavigateUp: (() -> Unit)? = null,
    // أضفنا هذه الأسطر بقيم افتراضية لضمان عدم تأثر الشاشات الأخرى
    height: androidx.compose.ui.unit.Dp = 100.dp,
    bottomCorner: androidx.compose.ui.unit.Dp = 20.dp,
    expandableContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height) // سيأخذ 100dp في كل التطبيق إلا لو حددنا غير ذلك
            .clip(RoundedCornerShape(bottomStart = bottomCorner, bottomEnd = bottomCorner))
            .background(AppPrimary), contentAlignment = Alignment.TopCenter
    ) {
        if (onNavigateUp != null) {
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 45.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Cairo
            )

            // هذا الجزء سيظهر فقط في شاشة الدردشة عندما نمرر له محتوى
            expandableContent?.invoke()
        }
    }
}