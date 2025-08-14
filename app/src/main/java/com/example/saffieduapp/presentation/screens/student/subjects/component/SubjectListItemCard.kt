package com.example.saffieduapp.presentation.screens.student.subjects.component

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
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.ui.theme.Cairo
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import androidx.compose.foundation.layout.IntrinsicSize // مهم لـ height(IntrinsicSize.Min)

@Composable
fun SubjectListItemCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRatingChanged: (Int) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(IntrinsicSize.Min), // يضمن امتلاء الارتفاع لعناصر اليمين
            verticalAlignment = Alignment.Top
        ) {
            // العنصر الأول (يسار): صف الصورة + تفاصيل المادة
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = subject.imageUrl,
                        contentDescription = subject.name,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.defultsubject),
                        error = painterResource(id = R.drawable.defultsubject)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${subject.lessonCount} دروس",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Cairo
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        fontFamily = Cairo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أ. ${subject.teacherName}",
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Cairo
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subject.grade,
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Cairo
                    )
                }
            }

            // العنصر الثاني (يمين): النجوم والتقييم بأسفل المساحة
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 120.dp), // مساحة مخصصة لليمين
                contentAlignment = Alignment.BottomStart // أسفل-يسار
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InteractiveRatingBar(
                        rating = subject.rating.toInt(),
                        onRatingChanged = onRatingChanged
                    )
                    Text(
                        text = subject.rating.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = Cairo
                    )
                }
            }
        }
    }
}
