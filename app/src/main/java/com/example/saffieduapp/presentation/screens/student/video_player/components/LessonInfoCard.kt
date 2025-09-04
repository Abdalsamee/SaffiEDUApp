package com.example.saffieduapp.presentation.screens.student.video_player.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun LessonInfoCard(
    title: String,
    description: String,
    teacherName: String,
    teacherImageUrl: String,
    duration: String,
    publicationDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ١. عنوان الدرس
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ٢. معلومات المعلم
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = teacherImageUrl,
                    contentDescription = teacherName,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.defultsubject),
                    error = painterResource(id = R.drawable.defultsubject)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = teacherName, fontWeight = FontWeight.SemiBold)
                    Text(text = "$duration دقيقة", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    Text(text = "تاريخ النشر : $publicationDate", color = Color.Black, fontSize = 14.sp,fontWeight = FontWeight.Normal)
                }
            }


            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // ٤. الوصف
            Text(text = "الوصف:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description)
        }
    }
}


@Preview(showBackground = true,locale = "ar")
@Composable
private fun LessonInfoCardPreview() {

    SaffiEDUAppTheme {
        LessonInfoCard(
            title = "الدرس الأول: الكسور",
            description = "هذا الفيديو يشرح قوانين الكسور بشكل مبسط وسهل الفهم للطلاب.",
            teacherName = "أ. عبدالسميع النجار",
            teacherImageUrl = "", // الرابط ليس مهمًا في المعاينة
            duration = "20",
            publicationDate = "7/7/2025"
        )
    }
}