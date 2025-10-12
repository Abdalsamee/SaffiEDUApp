package com.example.saffieduapp.presentation.screens.teacher.tasks.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import com.example.saffieduapp.R

@Composable
fun StudentTaskItemCard(
    name: String,
    score: String,
    imageUrl: String?,
    onDetailsClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // صورة الطالب (مع معالجة الخطأ)
            AsyncImage(
                model = imageUrl ?: "",
                contentDescription = "صورة الطالب",
                placeholder = painterResource(R.drawable.secstudent),
                error = painterResource(R.drawable.secstudent),
                fallback = painterResource(R.drawable.secstudent),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            // الاسم والدرجة
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    color = Color(0xFF1C1C1C),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
                Text(
                    text = "الدرجة: $score",
                    color = Color.Gray,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }

            Button(
                onClick = onDetailsClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                modifier = Modifier.height(36.dp)
            ) {
                Text("التفاصيل", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false, locale = "ar")
@Composable
fun StudentTaskItemCardPreview() {
    SaffiEDUAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // بطاقة بصورة صحيحة
            StudentTaskItemCard(
                name = "محمد أحمد",
                score = "95/100",
                imageUrl = "https://randomuser.me/api/portraits/men/75.jpg",
                onDetailsClick = {}
            )

            // بطاقة بدون صورة (تظهر الصورة الافتراضية)
            StudentTaskItemCard(
                name = "أحمد يوسف",
                score = "88/100",
                imageUrl = null,
                onDetailsClick = {}
            )
        }
    }
}
