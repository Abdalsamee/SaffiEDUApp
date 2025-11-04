package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.assignmnet

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentAssignmentScreen(
    navController: NavController?,
    viewModel: TeacherStudentAssignmentViewModel = hiltViewModel(),
    studentId: String,
    assignmentId: String
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var expandedImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // تحميل بيانات الطالب وتسليمه
        viewModel.loadStudentAssignmentDetails(studentId, assignmentId)
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "عرض واجبات الطالب", onNavigateUp = { navController?.popBackStack() })
        }) { innerPadding ->
        TeacherStudentAssignmentContent(
            state = state,
            onGradeChange = viewModel::onGradeChange,
            onCommentChange = viewModel::onCommentChange,
            onSave = viewModel::onSaveEvaluation,
            onFileClick = { file ->
                if (file.isImage) {
                    expandedImageUrl = file.fileUrl
                } else {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(file.fileUrl), "application/pdf")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }

    // 🔹 Dialog لعرض الصورة المكبّرة
    if (expandedImageUrl != null) {
        Dialog(onDismissRequest = { expandedImageUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(expandedImageUrl)
                        .crossfade(true).build(),
                    contentDescription = "عرض الصورة",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )

                IconButton(
                    onClick = { expandedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherStudentAssignmentContent(
    state: TeacherStudentAssignmentState,
    onGradeChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSave: () -> Unit,
    onFileClick: (SubmittedFile) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppPrimary)
        }
        return
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoRow(label = "الصف :", value = state.studentClass)
        InfoRow(label = "حالة التسليم :", value = state.deliveryStatus)

        Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)

        Text(
            text = "الملفات المسلّمة :",
            color = AppTextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        if (state.submittedFiles.isEmpty()) {
            Text("لا توجد ملفات مسلّمة.", color = Color.Gray, fontSize = 15.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.submittedFiles.forEach { file ->
                    SubmittedFileItem(file = file, onClick = { onFileClick(file) })
                }
            }
        }

        Divider(color = Color(0xFFE5E5E5), thickness = 1.dp)

        Text(
            text = "التقييم والتعليق :",
            color = AppTextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        OutlinedTextField(
            value = state.grade,
            onValueChange = onGradeChange,
            label = { Text("التقييم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = state.comment,
            onValueChange = onCommentChange,
            label = { Text("تعليق") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("حفظ", fontSize = 17.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = AppTextSecondary, modifier = Modifier.width(120.dp))
        Text(
            value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubmittedFileItem(file: SubmittedFile, onClick: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (file.isImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(file.fileUrl)
                        .crossfade(true).build(),
                    contentDescription = "صورة الواجب",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "ملف PDF",
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    color = AppPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = if (file.isImage) "صورة" else "PDF",
                    color = AppTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
fun PreviewTeacherStudentAssignmentScreen() {
    SaffiEDUAppTheme {
        val previewState = TeacherStudentAssignmentState(
            isLoading = false,
            studentClass = "الصف السادس",
            deliveryStatus = "تم التسليم",
            submittedFiles = listOf(
                SubmittedFile(
                    "واجب اللغة العربية.pdf",
                    "https://www.africau.edu/images/default/sample.pdf",
                    false
                ), SubmittedFile("صورة الواجب 1", "https://picsum.photos/300", true)
            ),
            grade = "95",
            comment = "عمل ممتاز"
        )

        TeacherStudentAssignmentContent(
            state = previewState,
            onGradeChange = {},
            onCommentChange = {},
            onSave = {},
            onFileClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
