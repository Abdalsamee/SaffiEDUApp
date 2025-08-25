package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
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
import com.example.saffieduapp.presentation.screens.teacher.home.TeacherClass
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.Cairo

@Composable
fun TeacherClassCard(
    teacherClass: TeacherClass,
    modifier: Modifier = Modifier,
    subjectName: String,
    onClick: () -> Unit
) {
    Card(
        // ١. جعل البطاقة أعرض قليلاً
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = teacherClass.subjectImageUrl,
                    contentDescription = "Subject Image",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.defultsubject),
                    error = painterResource(R.drawable.defultsubject),
                )
                Column {
                    Text(text = teacherClass.className, fontWeight = FontWeight.Bold)
                    Text(text = subjectName, fontSize = 14.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(width = 90.dp, height = 34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                ) {
                    Text(
                        text = "التفاصيل",
                        color = Color.White,
                        fontFamily = Cairo,
                        fontSize = 11.sp
                    )
                }

                // --- صور الطلاب المتداخلة مع العدد ---
                Box(
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp)
                    ) {
                        teacherClass.studentImages.take(3).forEach { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Student",
                                error = painterResource(R.drawable.rectangle),
                                placeholder = painterResource(R.drawable.rectangle),
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                    if (teacherClass.studentCount > 3) {
                        // ٢. تصميم مربع عدد الطلاب
                        Box(
                            modifier = Modifier
                                .padding(start = (2 * 18).dp + 12.dp)
                                .size(32.dp) // الحجم المطلوب
                                .clip(RoundedCornerShape(12.dp)) // الحواف المطلوبة
                                .background(Color.Black) // اللون المطلوب
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp)), // البوردر
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${teacherClass.studentCount - 3}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}