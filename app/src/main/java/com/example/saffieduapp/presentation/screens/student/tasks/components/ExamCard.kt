package com.example.saffieduapp.presentation.screens.student.tasks.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExamCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    time: Long,
    status: String,
    isCompleted: Boolean,
    imageResId: Int,
    onclick: () -> Unit
) {
    val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(time))

    val cardBackgroundColor = if (isCompleted) Color(0xFFFAD6B7) else CardBackgroundColor
    val statusTextColor = if (isCompleted) Color(0xFF6FCF97) else Color(0xFF828282)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.width(70.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedTime,
                style = Typography.bodyMedium,
                color = AppTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.W600
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = status,
                style = Typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = statusTextColor,
                fontSize = 12.sp,
            )
        }


        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // صورة الدرس
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "Lesson Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.End,
                        fontSize = 13.sp,
                        color = AppTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = Typography.bodyLarge,
                        textAlign = TextAlign.End,
                        color = AppTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W400,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun IslamicLessonCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        // حالة "اكتمل"
        ExamCard(
            title = "اختبار الوحدة الثانية",
            subtitle = "مادة التربية الإسلامية",
            time = System.currentTimeMillis(),
            status = "اكتمل",
            isCompleted = true,
            imageResId = R.drawable.defultsubject,
            onclick = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        // حالة "لم يكتمل"
        ExamCard(
            title = "درس جديد في الفقه",
            subtitle = "أحكام الصلاة",
            time = System.currentTimeMillis() + 3600000,
            status = "لم يكتمل",
            isCompleted = false,
            imageResId = R.drawable.defultsubject,
            onclick = {}
        )
    }
}

