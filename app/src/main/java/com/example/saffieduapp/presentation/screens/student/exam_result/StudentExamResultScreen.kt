package com.example.saffieduapp.presentation.screens.student.exam_result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun StudentExamResultScreen(
    navController: NavController,
    examId: String,
    onNavigateUp: () -> Unit = { navController.popBackStack() },
    viewModel: StudentExamResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // ✅ تحميل البيانات عند فتح الشاشة
    LaunchedEffect(examId) {
        viewModel.loadExamResult(examId)
    }

    StudentExamResultScreenContent(
        state = state, onNavigateUp = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentExamResultScreenContent(
    state: StudentExamResultState, onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "النتيجة", onNavigateUp = onNavigateUp
            )
        }) { innerPadding ->
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // 🔹 بطاقة المعلومات (الاختبار والمادة)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD3E5FF), RoundedCornerShape(12.dp))
                        .padding(vertical = 16.dp), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.examTitle,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.subjectName,
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val shouldShowScore = state.isGraded && state.showResultsImmediately
                // ✅ تحقق من حالة التقييم
                if (shouldShowScore) {
                    // 🔸 في حال لم يتم التقييم بعد
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3CD), RoundedCornerShape(12.dp))
                            .padding(24.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يتم تقييم الاختبار بعد، ستظهر نتيجتك هنا عند الانتهاء من التصحيح.",
                            color = Color(0xFF856404),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // 🔹 عرض النتيجة
                    Text(
                        text = "الدرجة هي :",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .background(AppPrimary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 40.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "${state.earnedScore}/${state.totalScore}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
private fun PreviewStudentExamResult_NotGraded() {
    SaffiEDUAppTheme {
        StudentExamResultScreenContent(
            state = StudentExamResultState(
                isLoading = false,
                examTitle = "اختبار الوحدة الثانية",
                subjectName = "مادة التربية الإسلامية",
                totalScore = "15",
                earnedScore = "7",
                isGraded = false
            ), onNavigateUp = {})
    }
}

