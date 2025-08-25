package com.example.saffieduapp.presentation.screens.teacher.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.saffieduapp.presentation.screens.teacher.home.StudentUpdate
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun StudentUpdateCard(
    update: StudentUpdate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // زيادة الارتفاع لإفساح مجال للصورة الكبيرة
    ) {
        // البطاقة (الخلفية)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // البطاقة في الأسفل
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier.background(//EBB34F
                    Brush.horizontalGradient(//
                        colors = listOf(Color(0xFFEBB34F), Color(0xFFFAEACF))
                    )
                )
            ) {
                // المحتوى داخل البطاقة
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // الصورة المصغرة + النصوص في عمود
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        AsyncImage(
                            model = update.studentImageUrl,
                            contentDescription = "Student Image",
                            placeholder = painterResource(R.drawable.rectangle),
                            error = painterResource(R.drawable.rectangle),
                            modifier = Modifier
                                .size(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = update.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(text = update.taskTitle, fontSize = 14.sp)

                        }
                        Text(text = update.submissionTime, fontSize = 12.sp, color = Color.DarkGray)

                    }

                    // الزر أسفل البطاقة
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .width(120.dp)
                            .height(30.dp)
                            .align(Alignment.Start),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "التفاصيل", color = Color.White, fontSize = 13.sp)
                    }
                }


            }}

                // الصورة الكبيرة
        Image(
            painter = painterResource(id = R.drawable.studentup),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .align(Alignment.BottomEnd) // بداية البطاقة (اليسار/الأعلى)
                .offset( y = 0.dp) // رفع الصورة للأعلى قليلاً
                .height(230.dp) // حجم كبير مشابه للصورة المرجعية
        )
    }
}
