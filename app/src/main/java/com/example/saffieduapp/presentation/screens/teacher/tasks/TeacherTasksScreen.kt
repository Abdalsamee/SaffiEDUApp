package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.ClassFilterButton
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.TeacherTaskCard
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@Composable
fun TeacherTasksScreen(
    navController: NavController,
    viewModel: TeacherTasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabTitles = listOf("الواجبات", "الاختبارات")

    Scaffold(
        topBar = { CommonTopAppBar(title = "المهام") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // 🔹 زر الفلترة "الصف السادس"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                ClassFilterButton(
                    selectedClass = state.selectedClass,
                    onClassSelected = { viewModel.onClassSelected(it) }
                )
            }

            // 🔹 التبويبات (الواجبات / الاختبارات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val textColor = if (state.selectedTabIndex == index) AppPrimary else AppTextSecondary
                    Text(
                        text = title,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable { viewModel.onTabSelected(index) }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 🔹 المحتوى حسب التبويب المحدد
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val list = if (state.selectedTabIndex == 0) state.assignments else state.exams
                    items(list) { task ->
                        TeacherTaskCard(
                            type = task.type,
                            subject = task.subject,
                            date = task.date,
                            time = task.time,
                            isActive = task.isActive,
                            onDetailsClick = { /* nav to details later */ },
                            onDeleteClick = { /* delete task later */ }
                        )
                    }
                }
            }
        }
    }
}
