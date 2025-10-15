package com.example.saffieduapp.presentation.screens.student.assignment_result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun StudentAssignmentResultScreen(
    navController: NavController? = null,
    viewModel: StudentAssignmentResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    StudentAssignmentResultScreenContent(
        state = state,
        onNavigateUp = { navController?.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentAssignmentResultScreenContent(
    state: StudentAssignmentResultState,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "عرض التقييم",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🔹 عرض الملفات
                state.files.forEach { file ->
                    Text(
                        text = file,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(thickness = 1.dp, color = Color.LightGray)

                // 🔹 بطاقة التقييم
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "اسم الطالب: ${state.studentName}",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "التقييم: ${state.grade}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                        Text(text = "التعليق:", fontWeight = FontWeight.Medium)
                        Text(
                            text = state.comment.ifEmpty { "لا يوجد تعليق" },
                            color = AppTextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, locale = "ar")
@Composable
private fun PreviewStudentAssignmentResultScreen() {
    SaffiEDUAppTheme {
        StudentAssignmentResultScreenContent(
            state = StudentAssignmentResultState(
                isLoading = false,
                assignmentTitle = "واجب اللغة العربية",
                studentName = "فتح عبد السميع النجار",
                files = listOf(
                    "pdf.120211726 واجب اللغة العربية",
                    "pdf.123 واجب اللغة العربية"
                ),
                grade = "10 / 10",
                comment = "حل رائع جدًا 🌟"
            ),
            onNavigateUp = {}
        )
    }
}
