package com.example.saffieduapp.presentation.screens.teacher.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.tasks.TeacherTasksTab
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@Composable
fun TaskTabs(
    selectedTab: TeacherTasksTab,
    onTabSelected: (TeacherTasksTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = AppPrimary.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem(
            title = "الواجبات",
            isSelected = selectedTab == TeacherTasksTab.HOMEWORKS,
            onClick = { onTabSelected(TeacherTasksTab.HOMEWORKS) }
        )
        TabItem(
            title = "الاختبارات",
            isSelected = selectedTab == TeacherTasksTab.EXAMS,
            onClick = { onTabSelected(TeacherTasksTab.EXAMS) }
        )
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected)
        AppPrimary.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.background

    val textColor = if (isSelected)
        AppPrimary
    else
        AppTextSecondary

    Box(
        modifier = Modifier

            .padding(horizontal = 4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}
