package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.teacher.home.TopStudent
import com.example.saffieduapp.ui.theme.Cairo

@Composable
fun TopStudentCard(
    student: TopStudent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A90E2), Color(0xFFC7D1DD))
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // الجزء الأيسر: الصورة والترتيب
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    AsyncImage(
                        model = student.studentImageUrl,
                        contentDescription = student.studentName,
                        modifier = Modifier
                            .size(65.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.fstsudent),
                        error = painterResource(id = R.drawable.secstudent)
                    )
                    // إشارة الترتيب
                    Image(
                        painter = painterResource(
                            id = when (student.rank) {
                                1 -> R.drawable.firstmedal
                                2 -> R.drawable.secondmedal
                                3 -> R.drawable.secondmedal
                                else -> R.drawable.star // يمكنك إضافة صورة افتراضية
                            }
                        ),
                        contentDescription = "Rank ${student.rank}",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = -6.dp, y = (-4).dp)
                            .size(28.dp)
                    )
                }

                // الوسط: اسم الطالب والمعدل
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(
                        text = student.studentName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = Cairo
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${student.overallProgress}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = Cairo
                        )
                        Image(
                            painter = painterResource(id = R.drawable.trophy),
                            contentDescription = "Progress",
                            modifier = Modifier.size(20.dp)
                        )

                    }
                }

                // اليمين: الإنجازات
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = "أنجز ${student.assignmentsProgress} واجبات",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = Cairo,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = "أنجز ${student.quizzesProgress} اختبارات",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = Cairo,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }
    }
}