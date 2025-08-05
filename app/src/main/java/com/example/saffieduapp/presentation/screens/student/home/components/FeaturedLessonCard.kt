package com.example.saffieduapp.presentation.screens.student.home.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.saffieduapp.presentation.screens.student.component.ProgressBarWithPercentage
import com.example.saffieduapp.presentation.screens.student.home.FeaturedLessonUiModel
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppSecondary


@Composable
fun FeaturedLessonCard(
    lesson: FeaturedLessonUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {



    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppSecondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 1. الجزء العلوي: الصورة والنصوص (محاذاة لليمين)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start // <-- التعديل هنا
            ) {
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = lesson.imageUrl,
                        contentDescription = lesson.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.defultsubject),
                        error = painterResource(id = R.drawable.defultsubject)
                    )
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Icon",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // عامود النصوص
                Column(
                    horizontalAlignment = Alignment.Start // محاذاة النصوص لليمين
                ) {
                    Text(
                        text = lesson.title,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = lesson.subject,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }


            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = lesson.duration,
                    fontSize = 12.sp,
                    color = Color.Black
                )


                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppPrimary)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "مشاهدة",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ProgressBarWithPercentage(
                progress = lesson.progress,
                modifier = Modifier.fillMaxWidth()
            )
            
        }
    }
}