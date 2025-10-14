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
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components.*
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
        when {
            state.isLoading -> {
                // 🔹 أثناء التحميل
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                // 🔹 في حالة الخطأ
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "حدث خطأ غير متوقع",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
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
                        studentImageUrl = state.studentImageUrl ?: "",
                        onSaveClick = viewModel::onSaveExamEvaluation
                    )

                    // 🔹 قسم التقييم (الدرجة، الإجابات، الوقت، الحالة)
                    ExamEvaluationSection(
                        earnedScore = state.earnedScore.toString(),
                        totalScore = state.totalScore.toString(),
                        onScoreChange = viewModel::onScoreChange,
                        answerStatus = state.answerStatus,
                        totalTime = "${state.totalTimeMinutes} دقيقة",
                        examStatus = when (state.examStatus) {
                            ExamStatus.COMPLETED -> "مكتمل"
                            ExamStatus.IN_PROGRESS -> "قيد التقدم"
                            ExamStatus.EXCLUDED -> "مستبعد"
                        },
                        onViewAnswersClick = viewModel::onViewAnswersClick
                    )

                    // 🔹 قسم محاولات الغش
                    if (state.cheatingLogs.isNotEmpty()) {
                        CheatingLogsSection(logs = state.cheatingLogs)
                    }

                    // 🔹 قسم الوسائط (الصور + الفيديو)
                    ExamMediaSection(
                        imageUrls = state.imageUrls ?: emptyList(),
                        videoUrl = state.videoUrl,
                        onImageClick = viewModel::onImageClick,
                        onVideoClick = viewModel::onVideoClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
private fun PreviewTeacherStudentExamScreen() {
    SaffiEDUAppTheme {
        TeacherStudentExamScreen_PreviewOnly()
    }
}

@Composable
private fun TeacherStudentExamScreen_PreviewOnly() {
    val fakeState = TeacherStudentExamState(
        isLoading = false,
        studentName = "يزن عادل ظهير",
        studentImageUrl = "https://randomuser.me/api/portraits/men/60.jpg",
        earnedScore = 15,
        totalScore = 20,
        answerStatus = "مكتملة",
        totalTimeMinutes = 45,
        examStatus = ExamStatus.EXCLUDED,
        cheatingLogs = listOf(
            "10:05 ص → خرج من التطبيق (تنبيه)",
            "10:15 ص → أوقف الكاميرا",
            "10:20 ص → عودة للامتحان"
        ),
        imageUrls = listOf(
            "https://picsum.photos/200/300",
            "https://picsum.photos/200/301"
        ),
        videoUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"
    )

    Scaffold(topBar = { CommonTopAppBar(title = "نظام المراقبة") }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StudentHeaderRow(
                studentName = fakeState.studentName,
                studentImageUrl = fakeState.studentImageUrl ?: "",
                onSaveClick = {}
            )

            ExamEvaluationSection(
                earnedScore = fakeState.earnedScore.toString(),
                totalScore = fakeState.totalScore.toString(),
                onScoreChange = {},
                answerStatus = fakeState.answerStatus,
                totalTime = "${fakeState.totalTimeMinutes} دقيقة",
                examStatus = "مستبعد",
                onViewAnswersClick = {}
            )

            CheatingLogsSection(logs = fakeState.cheatingLogs)

            ExamMediaSection(
                imageUrls = fakeState.imageUrls ?: emptyList(),
                videoUrl = fakeState.videoUrl
            )
        }
    }
}
