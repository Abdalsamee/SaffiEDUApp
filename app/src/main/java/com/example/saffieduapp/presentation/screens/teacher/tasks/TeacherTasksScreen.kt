package com.example.saffieduapp.presentation.screens.teacher.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.tasks.components.ClassFilterButton
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherTasksScreen(
    navController: NavController,
    viewModel: TeacherTasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val tabTitles = listOf("الواجبات", "الاختبارات")

    Scaffold(
        topBar = {
            CommonTopAppBar(title = "المهام")
        }
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
                var selectedClass by remember { mutableStateOf("الصف السادس") }

                ClassFilterButton(
                    selectedClass = selectedClass,
                    onClassSelected = { selectedClass = it }
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
                    TeacherCustomTab(
                        text = title,
                        isSelected = state.selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 المحتوى حسب التبويب المحدد
            when (state.selectedTabIndex) {
                0 -> TasksHomeworkPlaceholder()
                1 -> TasksExamPlaceholder()
            }
        }
    }
}

@Composable
private fun TeacherCustomTab(
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



// placeholders مؤقتة حتى نكمل الشاشات الفرعية
@Composable
private fun TasksHomeworkPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("واجهة الواجبات للمعلم (قريباً)", color = AppTextSecondary)
    }
}

@Composable
private fun TasksExamPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("واجهة الاختبارات للمعلم (قريباً)", color = AppTextSecondary)
    }
}


