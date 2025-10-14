package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.R
@Composable
fun StudentHeaderRow(
    studentName: String,
    studentImageUrl: String,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 1) صورة الطالب (دائرية)
        AsyncImage(
            model = studentImageUrl,
            contentDescription = "صورة الطالب",
            placeholder = painterResource(R.drawable.secstudent),
            error = painterResource(R.drawable.secstudent),
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            tonalElevation = 0.dp,
            modifier = Modifier
                .weight(0.8f)
        ) {
            Text(
                text = studentName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        // 3) زر الحفظ (ثابت العرض ليظهر كزر واضح)
        Button(
            onClick = onSaveClick,
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .widthIn(min = 88.dp) // عرض مناسب
                .height(40.dp)
        ) {
            Text("حفظ", fontSize = 14.sp , color = Color.White)
        }
    }
}