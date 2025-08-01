package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.saffieduapp.presentation.screens.student.home.FeaturedLessonUiModel

@Composable
fun FeaturedLessonsSection(
    lessons: List<FeaturedLessonUiModel>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = "أهم الدروس",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(lessons) { lesson ->
                FeaturedLessonCard(lesson = lesson)
            }
        }
    }
}