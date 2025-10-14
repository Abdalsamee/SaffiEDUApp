package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components.ExamEvaluationSection
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components.StudentHeaderRow
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentExamScreen(
    navController: NavController? = null,
    viewModel: TeacherStudentExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "نظام المراقبة",
                onNavigateUp = { navController?.popBackStack() }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            // 🔹 أثناء التحميل
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 🔹 المحتوى الرئيسي
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🔹 الصف العلوي (صورة الطالب + الاسم + زر الحفظ)
                StudentHeaderRow(
                    studentName = state.studentName,
                    studentImageUrl = state.studentImageUrl,
                    onSaveClick = viewModel::onSaveExamEvaluation
                )

                // 🔹 قسم التقييم (الدرجة، الإجابات، الوقت، الحالة)
                ExamEvaluationSection(
                    earnedScore = state.earnedScore,
                    totalScore = state.totalScore,
                    onScoreChange = viewModel::onScoreChange,
                    answerStatus = state.answerStatus,
                    totalTime = state.totalTime,
                    examStatus = state.examStatus,
                    onViewAnswersClick = viewModel::onViewAnswersClick
                )


            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
private fun PreviewTeacherStudentExamScreen() {
    SaffiEDUAppTheme {
        Scaffold(topBar = { CommonTopAppBar(title = "نظام المراقبة") }) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StudentHeaderRow(
                    studentName = "يزن عادل ظهير",
                    studentImageUrl = "https://randomuser.me/api/portraits/men/60.jpg",
                    onSaveClick = {}
                )
                ExamEvaluationSection(
                    earnedScore = "15",
                    totalScore = "20",
                    onScoreChange = {},
                    answerStatus = "مكتملة",
                    totalTime = "45 دقيقة",
                    examStatus = "مستبعد",
                    onViewAnswersClick = {}
                )
            }
        }
    }
}

