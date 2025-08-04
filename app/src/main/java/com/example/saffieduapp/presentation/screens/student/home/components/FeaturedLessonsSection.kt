package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.home.FeaturedLessonUiModel

@Composable
fun FeaturedLessonsSection(
    lessons: List<FeaturedLessonUiModel>,
    modifier: Modifier = Modifier,
    onFeaturedLessonClick: (lessonId: String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "أهم الدروس",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                color = Color.Black
            )
            Text(
                text = "المزيد",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                color = Color.Black,
                modifier = Modifier.clickable {
                    //تنفيذ الانتقال الى واجهة الدروس
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (lessons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد دروس متاحة حالياً",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(lessons) { lesson ->
                    FeaturedLessonCard(
                        lesson = lesson,
                        onClick = { onFeaturedLessonClick(lesson.id) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}