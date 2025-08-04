package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.saffieduapp.presentation.screens.student.home.EnrolledSubjectUiModel
import com.example.saffieduapp.ui.theme.AppSecondary

@Composable
fun EnrolledSubjectCard(
    subject: EnrolledSubjectUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        // 1. تحديد الأبعاد والشكل الخارجي للبطاقة
        modifier = modifier
            .width(150.dp)
            .height(180.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppSecondary
        )
    ) {
        // 2. استخدام Column لترتيب العناصر عمودياً
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 3. الجزء العلوي: الصورة بجانب اسم المادة
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AsyncImage(
                    model = subject.imageUrl,
                    contentDescription = subject.name,
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.defultsubject),
                    error = painterResource(id = R.drawable.defultsubject)
                )
                Text(
                    text = subject.name,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color.Black
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. الجزء الأوسط: اسم المدرس والصف
            Column {
                Text(
                    text = "أ. ${subject.teacherName}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = subject.grade,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // هذا الـ Spacer سيأخذ كل المساحة المتبقية لدفع التقييم للأسفل
            Spacer(modifier = Modifier.weight(1f))

            // 5. الجزء السفلي: التقييم
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subject.rating.toString(),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Icon(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107), // لون أصفر
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}