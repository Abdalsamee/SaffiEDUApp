package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.home.TeacherClass

@Composable
fun TeacherClassesSection(
    classes: List<TeacherClass>,
    modifier: Modifier = Modifier,
    onClassClick: (classId: String) -> Unit,
    subjectName: String,
    onMoreClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "صفوفي", fontSize = 16.sp, color = Color.Black)
            Text(
                text = "المزيد",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(classes) { teacherClass ->
                TeacherClassCard(
                    teacherClass = teacherClass,
                    subjectName = subjectName,
                    onClick = { onClassClick(teacherClass.classId) }
                )
            }
        }
    }
}