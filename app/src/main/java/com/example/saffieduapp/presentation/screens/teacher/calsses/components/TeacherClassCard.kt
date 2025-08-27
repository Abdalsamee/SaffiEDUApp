package com.example.saffieduapp.presentation.screens.teacher.calsses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.saffieduapp.presentation.screens.teacher.calsses.ClassItem
import com.example.saffieduapp.ui.theme.AppPrimary


@Composable
fun TeacherClassCard(
    classItem: ClassItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = AppPrimary,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- القسم العلوي ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = classItem.className,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = classItem.subjectName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                AsyncImage(
                    model = classItem.subjectImageUrl,
                    contentDescription = "Subject Image",
                    modifier = Modifier
                        .size(55.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = 2.dp,
                            color = AppPrimary,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.defultsubject),
                    error = painterResource(id = R.drawable.defultsubject)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // --- قسم الإحصائيات ---
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatRow(count = classItem.quizCount, label = "عدد الاختبارات")
                StatRow(count = classItem.assignmentCount, label = "عدد الواجبات")
                StatRow(count = classItem.videoLessonCount, label = "عدد الدروس المصورة")
                StatRow(count = classItem.pdfLessonCount, label = "عدد ملفات PDF")
                StatRow(count = classItem.studentCount, label = "عدد الطلاب")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- زر التفاصيل ---
            Button(
                onClick = onClick,
                modifier = Modifier.wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                Text(
                    text = "تفاصيل الفصل",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatRow(count: Int, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.DarkGray,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
