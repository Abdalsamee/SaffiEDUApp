package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentExamScreen(
    navController: NavController?,
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
        TeacherStudentExamContentBasic(
            state = state,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

/** محتوى مبدئي (نصي فقط) — سنضيف الوسائط والأزرار في الخطوة 2 */
@Composable
private fun TeacherStudentExamContentBasic(
    state: TeacherStudentExamState,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // اسم الطالب + صورته
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // الاسم كبطاقة صغيرة
            AssistChip(
                onClick = { },
                label = { Text(state.studentName) }
            )

            if (!state.studentAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = state.studentAvatarUrl,
                    contentDescription = "صورة الطالب",
                    modifier = Modifier
                        .size(56.dp),
                )
            }
        }

        // معلومات النتيجة/المحاولة
        InfoLine("الدرجة المستحقة :", state.earnedScore)
        InfoLine("حالة الإجابات :", state.answersStatus)
        InfoLine("الوقت الكلي للمحاولة :", state.totalAttemptTime)
        InfoLine("الحالة :", state.overallStatus)

        // محاولات الغش (نصية فقط الآن)
        Text(text = "محاولات الغش :", color = AppTextSecondary, fontWeight = FontWeight.Medium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.cheatingLogs.forEach { log ->
                Text(text = "• $log")
            }
        }

        // سنضيف لاحقاً:
        // - قسم لقطات المراقبة (صور + فيديو) مع التفاعلات
        // - الأزرار (مشاهدة الإجابات/حفظ)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = AppTextSecondary)
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}

/* Preview باستخدام حالة وهمية دون ViewModel */
@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
private fun PreviewTeacherStudentExamScreen_Basic() {
    val previewState = TeacherStudentExamState(
        isLoading = false,
        studentName = "يزن عادل ظهر",
        studentAvatarUrl = "https://picsum.photos/200",
        earnedScore = "15 من 20",
        answersStatus = "مكتملة",
        totalAttemptTime = "45 دقيقة",
        overallStatus = "مستبعد",
        cheatingLogs = listOf(
            "10:05 ص → خرج من التطبيق (تنبيه)",
            "10:15 ص → أوقف الكاميرا",
            "10:20 ص → عودة للامتحان"
        )
    )
    SaffiEDUAppTheme {
        TeacherStudentExamContentBasic(state = previewState, modifier = Modifier.fillMaxSize())
    }
}
