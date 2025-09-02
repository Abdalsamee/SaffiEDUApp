package com.example.saffieduapp.presentation.screens.student.subject_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.subject_details.components.Lesson
import com.example.saffieduapp.presentation.screens.student.subject_details.components.LessonCard

@Composable
fun LessonsGridContent(lessons: List<Lesson>) {

    if (lessons.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد دروس متاحة حاليًا.")
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // تحديد عدد الأعمدة بـ 2
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // المسافة الأفقية بين العناصر
        verticalArrangement = Arrangement.spacedBy(12.dp)  // المسافة الرأسية بين العناصر
    ) {
        items(
            items = lessons,
            key = { lesson -> lesson.id }
        ) { lesson ->
            // استدعاء البطاقة مباشرةً مع العنصر الحالي
            LessonCard(lesson = lesson)
        }
    }
}