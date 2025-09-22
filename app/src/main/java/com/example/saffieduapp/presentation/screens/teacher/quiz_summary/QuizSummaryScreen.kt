package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.quiz_summary.components.QuestionSummaryItem
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSummaryScreen(
    onNavigateUp: () -> Unit,
    onPublish:() ->Unit
    // TODO: Add ViewModel
) {
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // ديالوج تأكيد الحذف
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من أنك تريد حذف هذا السؤال؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Handle delete confirmation
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "عنوان الاختبار",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("السؤال الأوdfdfdfdffdxdwcل", "dfdfdfdffdfالسؤال الثاني", "dfvffvgالسؤال الثالث")) { question ->
                    QuestionSummaryItem(
                        questionText = question,
                        onEditClick = { /* TODO: Navigate to edit question */ },
                        onDeleteClick = { showDeleteConfirmationDialog = true }
                    )
                }
            }

            AppButton(
                text = "نشر الاختبار",
                onClick = onPublish,
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

        }
    }
}


//@Preview(showBackground = true, locale = "ar")
//@Composable
//private fun QuizSummaryScreenPreview() {
//    SaffiEDUAppTheme {
//        QuizSummaryScreen(onNavigateUp = {})
//    }
//}