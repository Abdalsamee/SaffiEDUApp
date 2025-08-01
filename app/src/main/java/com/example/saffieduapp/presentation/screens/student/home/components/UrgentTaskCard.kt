package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.home.UrgentTaskUiModel

@Composable
fun UrgentTaskCard(
    task: UrgentTaskUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth() // تحديد عرض البطاقة
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = task.title)
                Text(text = task.subject)
                Text(text = "تاريخ الانتهاء: ${task.dueDate}")
            }
        }
    }
}