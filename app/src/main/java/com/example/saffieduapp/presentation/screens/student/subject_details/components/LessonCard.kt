package com.example.saffieduapp.presentation.screens.student.subject_details.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.component.ProgressBarWithPercentage
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import kotlin.math.roundToInt

@Composable
fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit,

) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // الهيكل الرئيسي عمودي لوضع العناصر فوق بعضها
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // --- الصف العلوي: الصورة والعناوين ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- الصورة مع أيقونة التشغيل ---
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = lesson.videoUrl, // 1. الرابط الفعلي للصورة
                        contentDescription = lesson.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.rectangle), // 2. صورة مؤقتة أثناء التحميل
                        error = painterResource(id = R.drawable.rectangle) // 3. صورة بديلة في حال حدوث خطأ
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.5f)), // أبيض شفاف 50%
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // --- العنوان الرئيسي والفرعي ---
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,

                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = AppTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = lesson.subTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = AppTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- الصف الأوسط: الزمن وزر المشاهدة ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الزمن ${lesson.duration} دقيقة",
                    fontSize = 10.sp,
                    color = Color.Black
                )
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "مشاهدة", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProgressBarWithPercentage(
                progress = lesson.progress.toInt(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


