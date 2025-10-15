package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam_answers.components.AnswerItemCard
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentExamAnswersScreen(
    navController: NavController? = null,
    viewModel: TeacherStudentExamAnswersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "عرض اجابات الطالب",
                onNavigateUp = { navController?.popBackStack() }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
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
                state.answers.forEach { answer ->
                    AnswerItemCard(
                        answer = answer,
                        onScoreSelected = { newScore ->
                            viewModel.onScoreSelected(answer.questionId, newScore)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    "حفظ",
                    onClick = { viewModel.onSaveGrades() },
                    modifier = Modifier
                        .fillMaxWidth()

                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
fun PreviewTeacherStudentExamAnswersScreen() {
    SaffiEDUAppTheme {
        TeacherStudentExamAnswersScreen_PreviewOnly()
    }
}

@Composable
private fun TeacherStudentExamAnswersScreen_PreviewOnly() {
    val fakeState = TeacherStudentExamAnswersState(
        isLoading = false,
        answers = listOf(
            StudentAnswer(
                questionId = "q1",
                questionText = "نص السؤال الأول",
                answerText = "نص الإجابة التي اختارها الطالب",
                questionType = QuestionType.SINGLE_CHOICE,
                maxScore = 5,
                assignedScore = 3
            ),
            StudentAnswer(
                questionId = "q2",
                questionText = "نص السؤال الثاني",
                answerText = "نص الإجابة التي اختارها الطالب",
                questionType = QuestionType.TRUE_FALSE,
                maxScore = 3,
                assignedScore = 2
            ),
            StudentAnswer(
                questionId = "q3",
                questionText = "نص السؤال الثالث",
                answerText = "نص الإجابة المقالية التي كتبها الطالب ....",
                questionType = QuestionType.ESSAY,
                maxScore = 10,
                assignedScore = null
            )
        ),
        totalScore = 5
    )

    Scaffold(topBar = { CommonTopAppBar(title = "عرض اختبار الطالب") }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fakeState.answers.forEach { answer ->
                AnswerItemCard(
                    answer = answer,
                    onScoreSelected = {}
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
            ) {
                Text("حفظ", color =Color.White)
            }
        }
    }
}

