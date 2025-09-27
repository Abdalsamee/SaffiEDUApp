package com.example.saffieduapp.presentation.screens.student.exam_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.exam_details.components.ExamInstructionsDialog
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.ui.theme.CardBackgroundColor
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun ExamDetailsScreen(
    onNavigateUp: () -> Unit,
    viewModel: ExamDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ExamDetailsScreenContent(
        state = state,
        onNavigateUp = onNavigateUp,
        onStartExamClick = { /* TODO */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamDetailsScreenContent(
    state: ExamDetailsState,
    onNavigateUp: () -> Unit,
    onStartExamClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        ExamInstructionsDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onStartExamClick()
            }
        )
    }
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = state.examDetails?.subjectName ?: "تفاصيل الاختبار",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.examDetails != null) {
            val details = state.examDetails
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = details.imageUrl,
                    contentDescription = details.subjectName,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .size(250.dp),
                    placeholder = painterResource(id = R.drawable.defultsubject),
                    error = painterResource(id = R.drawable.defultsubject)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xffFFEDDD))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = details.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = details.teacherName, color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = details.date)
                        Text(text = "يبدأ ${details.startTime}، ينتهي ${details.endTime}")
                        Text(text = "المدة ${details.durationInMinutes} دقيقة")
                        Text(text = "يحتوي الاختبار على ${details.questionCount} سؤال")

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppButton(
                                text = "بدء الاختبار الآن",
                                onClick =  { showDialog = true },
                                enabled = details.status == "متاح"
                            )
                            Text(
                                text = details.status,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp

                            )

                        }

                    }
                }


            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ExamDetailsScreenPreview() {
    SaffiEDUAppTheme {
        val previewState = ExamDetailsState(
            isLoading = false,
            examDetails = ExamDetails(
                id = "e1",
                title = "اختبار الوحدة الثانية",
                subjectName = "التربية الإسلامية",
                teacherName = "أ. فراس شعبان",
                imageUrl = "",
                date = "24 / 8 / 2025، الثلاثاء",
                startTime = "02:30 pm",
                endTime = "03:00 pm",
                durationInMinutes = 30,
                questionCount = 30,
                status = "متاح"
            )
        )
        ExamDetailsScreenContent(
            state = previewState,
            onNavigateUp = {},
            onStartExamClick = {}
        )
    }
}