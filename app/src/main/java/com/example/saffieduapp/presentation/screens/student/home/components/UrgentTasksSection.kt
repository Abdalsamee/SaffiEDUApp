package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.home.UrgentTaskUiModel

@Composable
fun UrgentTasksSection(
    tasks: List<UrgentTaskUiModel>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // المسافة بين البطاقات
    ) {
        // عنوان القسم
        Text(
            text = "المهام المستعجلة",
        )

        // عرض 3 بطاقات كحد أقصى
        tasks.take(3).forEach { tasks ->
            UrgentTaskCard(
                task = tasks,
                modifier = Modifier.fillMaxWidth() // جعل البطاقة تملأ العرض
            )
        }
    }
}