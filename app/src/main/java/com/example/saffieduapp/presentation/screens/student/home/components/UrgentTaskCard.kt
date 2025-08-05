package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.domain.model.UrgentTask // <-- ١. إضافة الاستدعاء الصحيح
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrgentTaskCard(
    task: UrgentTask, // <-- ٢. تم تصحيح النوع هنا
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAD6B7) // هذا هو اللون الثابت الذي حددته
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = task.imageUrl,
                contentDescription = task.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.defultsubject),
                error = painterResource(id = R.drawable.defultsubject)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.subject,
                    fontSize = 14.sp,
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,

                    ) {
                    Text(
                        text = "تاريخ الانتهاء: ${task.dueDate}",
                        fontSize = 10.sp,
                        color = AppTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "يبدأ الساعة ${task.startTime}",
                        fontSize = 10.sp,
                        color = AppTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}