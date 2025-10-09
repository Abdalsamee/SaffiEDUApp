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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                FilterButton("الصف السادس") {
                    // لاحقًا نفتح قائمة الفصول
                }
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

@Composable
private fun FilterButton(title: String, onClick: () -> Unit) {
    Surface(
        color = AppPrimary.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            color = AppPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
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
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewTeacherTasksScreen() {
    // نستخدم Theme التطبيق
    SaffiEDUAppTheme {
        // حالة وهمية للعرض فقط
        val fakeState = remember { mutableStateOf(0) }

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

                // 🔹 زر الفلترة (ثابت في المعاينة)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilterButton("الصف السادس") { }
                }

                // 🔹 التبويبات (محاكاة للتفاعل)
                val tabTitles = listOf("الواجبات", "الاختبارات")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        TeacherCustomTab(
                            text = title,
                            isSelected = fakeState.value == index,
                            onClick = { fakeState.value = index }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 🔹 محتوى وهمي حسب التبويب
                if (fakeState.value == 0) {
                    TasksHomeworkPlaceholder()
                } else {
                    TasksExamPlaceholder()
                }
            }
        }
    }
}