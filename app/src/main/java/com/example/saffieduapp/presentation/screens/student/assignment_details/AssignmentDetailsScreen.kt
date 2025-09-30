package com.example.saffieduapp.presentation.screens.student.assignment_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme


@Composable
fun AssignmentDetailsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToSubmit: (assignmentId: String) -> Unit,
    viewModel: AssignmentDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AssignmentDetailsScreenContent(
        state = state,
        onNavigateUp = onNavigateUp,
        onSubmitClick = {
            state.assignmentDetails?.let { details ->
                if (details.isSubmitEnabled) { // ← تحقق قبل التنقل
                    onNavigateToSubmit(details.id)
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentDetailsScreenContent(
    state: AssignmentDetailsState,
    onNavigateUp: () -> Unit,
    onSubmitClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = state.assignmentDetails?.subjectName ?: "تفاصيل الواجب",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.assignmentDetails != null) {
            val details = state.assignmentDetails
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ١. معلومات الواجب الأساسية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.defultsubject), // Placeholder
                        contentDescription = details.subjectName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = details.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text(text = details.teacherName, color = Color.Gray, fontSize = 16.sp)
                    }

                }

                Divider()

                details.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Assignment Image",
                        placeholder = painterResource(id = R.drawable.defultsubject),
                        error = painterResource(id = R.drawable.defultsubject),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }

                details.description?.let { description ->
                    Text(
                        text = "الوصف:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(text = description)
                }



                Spacer(modifier = Modifier.height(8.dp))

                // ٣. معلومات التسليم
                Text(text = details.dueDate)
                Text(text = details.remainingTime, color = Color.Gray)

                Spacer(modifier = Modifier.weight(1f))

                // ٤. زر التسليم
                AppButton(
                    text = "تسليم",
                    onClick = onSubmitClick,
                    enabled = details.isSubmitEnabled // ← الآن يعتمد على المهلة
                )
                if (!details.isSubmitEnabled && details.remainingTime == "منتهي") {
                    Text(
                        text = "انتهت مهلة تسليم الواجب",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun AssignmentDetailsScreenPreview() {
    SaffiEDUAppTheme {
        val previewState = AssignmentDetailsState(
            isLoading = false,
            assignmentDetails = AssignmentDetails(
                id = "a1",
                title = "النحو والصرف",
                description = "الرجاء حل التمارين في صفحة 55 وإرفاق صورة للحلول.",
                imageUrl = "android.resource://com.example.saffieduapp/${R.drawable.defultsubject}",
                subjectName = "اللغة العربية",
                teacherName = "أ. طاهر زياد قديح",
                dueDate = "ينتهي في: 10 أغسطس 2025 - 6:00 مساءً",
                remainingTime = "متبقي 10 أيام",
                isSubmitEnabled = true
            )
        )
        AssignmentDetailsScreenContent(
            state = previewState,
            onNavigateUp = {},
            onSubmitClick = {}
        )
    }
}