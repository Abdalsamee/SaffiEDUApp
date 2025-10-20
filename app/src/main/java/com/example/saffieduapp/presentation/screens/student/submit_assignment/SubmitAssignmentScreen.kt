package com.example.saffieduapp.presentation.screens.student.submit_assignment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.submit_assignment.components.NotesSection
import com.example.saffieduapp.presentation.screens.student.submit_assignment.components.SuccessDialog
import com.example.saffieduapp.ui.theme.AppAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAssignmentScreen(
    onNavigateUp: () -> Unit,
    viewModel: SubmitAssignmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> uris?.let { viewModel.addFiles(it) } }
    )

    // ✅ ديالوج النجاح
    if (state.submissionSuccess) {
        SuccessDialog(
            submitDate = state.submissionTime,
            onDismiss = {
                viewModel.resetSubmissionStatus()
                onNavigateUp()
            }
        )
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = state.assignmentTitle.ifEmpty { "تسليم الواجب" },
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // المحتوى الرئيسي
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ✅ اختيار الملفات فقط إذا لم يتم التسليم أو في وضع التعديل
                if (!state.alreadySubmitted || state.isEditingSubmission) {
                    FilePicker {
                        filePickerLauncher.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    }
                }

                // ✅ عرض الملفات المختارة
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.submittedFiles) { file ->
                        SubmittedFileItem(
                            file = file,
                            onRemoveClick = {
                                if (state.isEditingSubmission) viewModel.removeFile(file)
                            }
                        )
                    }
                }

                NotesSection()

                // ✅ الأزرار
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when {
                        !state.alreadySubmitted -> {
                            Button(
                                onClick = { viewModel.submitAssignment() },
                                modifier = Modifier.weight(1f),
                                enabled = state.submittedFiles.isNotEmpty() && !state.isSubmitting
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري التسليم...")
                                } else {
                                    Text("تسليم", color = Color.White)
                                }
                            }
                        }

                        state.alreadySubmitted && !state.isEditingSubmission -> {
                            Button(
                                onClick = { viewModel.toggleEditMode() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("عرض التسليم")
                            }
                        }

                        state.isEditingSubmission -> {
                            Button(
                                onClick = { viewModel.resubmitAssignment() },
                                modifier = Modifier.weight(1f)
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جارٍ التعديل...")
                                } else {
                                    Text("تعديل التسليم")
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.clearAllFiles() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppAlert,
                            contentColor = Color.White
                        ),
                        enabled = state.isEditingSubmission && !state.isSubmitting
                    ) {
                        Text("حذف")
                    }
                }
            }

            // ✅ طبقة تحميل شفافة أثناء الرفع (Overlay)
            if (state.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري رفع الملفات...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilePicker(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.plusfile),
                contentDescription = "Add File",
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "اختر الملف من جهازك", color = Color.Gray)
        }
    }
}

@Composable
private fun SubmittedFileItem(file: SubmittedFile, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(file.name, modifier = Modifier.weight(1f))
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Close, contentDescription = "Remove File", tint = Color.Gray)
            }
        }
    }
}