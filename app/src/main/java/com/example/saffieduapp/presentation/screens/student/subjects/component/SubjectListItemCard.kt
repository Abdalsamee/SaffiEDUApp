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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.ui.theme.Cairo
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

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
        // ✅ التغيير الرئيسي: استخدام Column كحاوية أساسية
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // --- الصف العلوي: للصورة والنصوص الأساسية ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // الصورة بحجم ثابت لمنع ضغطها
                AsyncImage(
                    model = subject.imageUrl.ifEmpty { null },
                    contentDescription = subject.name,
                    modifier = Modifier
                        .size(100.dp) // حجم ثابت وواضح
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.defultsubject),
                    error = painterResource(id = R.drawable.defultsubject)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // عمود النصوص يأخذ باقي المساحة المتاحة في الصف
                Column(modifier = Modifier.weight(1f)) {
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
                        color = Color.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal,
                        fontFamily = Cairo
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subject.grade,
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal,
                        fontFamily = Cairo
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // فاصل بين الصفين

            // --- الصف السفلي: لعدد الدروس والتقييم ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // عدد الدروس على اليسار
                Text(
                    text = "${subject.totalLessons ?: 0} دروس",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Cairo,
                    modifier=Modifier.padding(start=20.dp)
                )

                // فاصل مرن لدفع التقييم إلى اليمين
                Spacer(modifier = Modifier.weight(1f))

                // التقييم على اليمين
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${subject.rating ?: 0.0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = Cairo
                    )
                    InteractiveRatingBar(
                        rating = subject.rating?.toInt() ?: 0,
                        onRatingChanged = onRatingChanged
                    )

                }
            }
        }
    }
}

// --- دالة المعاينة (Preview) ---
@Preview(name = "New Layout Preview", showBackground = true,locale="ar")
@Composable
fun SubjectListItemCardPreview() {
    SaffiEDUAppTheme {
        val sampleSubject = Subject(
            id = "1",
            name = "الفيزياء الحديثة",
            teacherName = "أحمد علي",
            grade = "الصف الثاني عشر",
            imageUrl = "",
            totalLessons = 24,
            rating = 4.5f
        )
        SubjectListItemCard(
            subject = sampleSubject,
            onClick = {},
            onRatingChanged = {}
        )
    }
}