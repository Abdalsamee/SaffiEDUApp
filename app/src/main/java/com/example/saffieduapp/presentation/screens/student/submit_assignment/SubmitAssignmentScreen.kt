package com.example.saffieduapp.presentation.screens.student.submit_assignment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
            CommonTopAppBar(title = state.assignmentTitle, onNavigateUp = onNavigateUp)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.submittedFiles) { file ->
                    SubmittedFileItem(
                        file = file,
                        onRemoveClick = { if (state.isEditingSubmission) viewModel.removeFile(file) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            NotesSection()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    !state.alreadySubmitted -> {
                        Button(
                            onClick = { viewModel.submitAssignment() },
                            modifier = Modifier.weight(1f),
                            enabled = state.submittedFiles.isNotEmpty() && !state.isSubmitting
                        ) {
                            if (state.isSubmitting) CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            ) else Text("تسليم")
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
                            Text("تعديل التسليم")
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
                    enabled = state.isEditingSubmission
                ) {
                    Text("حذف")
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
                contentDescription = null,
                tint = Color.Gray
            )
            Text("اختر الملف من جهازك", color = Color.Gray)
        }
    }
}

@Composable
private fun SubmittedFileItem(file: SubmittedFile, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(file.name, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemoveClick) {
            Icon(Icons.Default.Close, contentDescription = "Remove")
        }
    }
}