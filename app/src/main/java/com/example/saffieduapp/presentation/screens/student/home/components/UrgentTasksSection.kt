package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.domain.model.UrgentTask

@Composable
fun UrgentTasksSection(
    tasks: List<UrgentTask>,
    modifier: Modifier = Modifier,
    onTaskClick: (taskId: String) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- Header Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "المهام المستعجلة",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                color = Color.Black
            )

            Text(
                text = "المزيد",
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                color = Color.Black,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }   // هنا ضع عرض قائمة المهام كالمعتاد
        tasks.forEach { task ->
            UrgentTaskCard(task = task, onClick = { onTaskClick(task.id) })
        }

        Spacer(modifier = Modifier.height(16.dp))




        if (tasks.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مهام مستعجلة حالياً",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                tasks.take(3).forEach { task ->
                    UrgentTaskCard(
                        task = task,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }
        }

    }
}
