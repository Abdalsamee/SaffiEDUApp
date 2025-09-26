package com.example.saffieduapp.presentation.screens.student.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.tasks.components.AssignmentCard
import com.example.saffieduapp.presentation.screens.student.tasks.components.ExamCard
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabTitles = listOf("الواجبات", "الاختبارات")

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "المهام")
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabTitles.forEachIndexed { index, title ->
                    CustomTab(
                        text = title,
                        isSelected = state.selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) }
                    )
                }
            }

            // Content
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // عرض القائمة المناسبة بناءً على التبويب المحدد
                if (state.selectedTabIndex == 0) {
                    AssignmentsList(assignmentsByDate = state.assignmentsByDate)
                } else {
                    ExamsList(examsByDate = state.examsByDate)
                }
            }
        }
    }
}

@Composable
private fun CustomTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) AppPrimary else AppTextSecondary
    Text(
        text = text,
        color = textColor,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}

// مكون خاص بقائمة الواجبات
@Composable
private fun AssignmentsList(assignmentsByDate: Map<String, List<AssignmentItem>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
    ) {
        assignmentsByDate.forEach { (date, assignments) ->
            item {
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // --- The Grid is built here manually using Row and chunked ---
            items(assignments.chunked(2)) { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { assignment ->
                        Box(modifier = Modifier.weight(1f)) {
                            AssignmentCard(
                                assignment = assignment,
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                    // Add a spacer to fill the gap if there's only one item in the last row
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// مكون خاص بقائمة الاختبارات
@Composable
private fun ExamsList(examsByDate: Map<String, List<ExamItem>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        examsByDate.forEach { (date, exams) ->
            item {
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(exams) { exam ->
                ExamCard(
                    title = exam.title,
                    subtitle = exam.subjectName,
                    time = exam.time,
                    status = if (exam.status == ExamStatus.COMPLETED) "اكتمل" else "لم يكتمل",
                    isCompleted = exam.status == ExamStatus.COMPLETED,
                    imageResId = R.drawable.defultsubject,
                    onclick = {
                        // TODO: Handle exam click
                        println("Exam clicked: ${exam.title}")
                    }
                )
            }
        }
    }
}