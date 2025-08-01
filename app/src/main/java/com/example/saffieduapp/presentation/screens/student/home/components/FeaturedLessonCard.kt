package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.home.FeaturedLessonUiModel

@Composable
fun FeaturedLessonCard(
    lesson: FeaturedLessonUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(220.dp) // تحديد عرض البطاقة
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Box for image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                // AsyncImage(model = lesson.imageUrl, ...)
                Text(text = "صورة الدرس")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = lesson.title)
            Text(text = lesson.subject)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { lesson.progress / 100f }, // تحويل النسبة المئوية إلى float
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}