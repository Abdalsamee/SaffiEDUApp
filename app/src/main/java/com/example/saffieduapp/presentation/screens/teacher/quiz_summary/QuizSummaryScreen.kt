package com.example.saffieduapp.presentation.screens.teacher.quiz_summary

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.data.FireBase.Exam
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamState
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.quiz_summary.components.QuestionSummaryItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSummaryScreen(
    onNavigateUp: () -> Unit,
    onPublish: () -> Unit,
    onEditQuestion: (QuestionData) -> Unit,
    examState: AddExamState,
    questions: List<QuestionData>,
    viewModel: QuizSummaryViewModel = hiltViewModel()
) {
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var questionToDelete by remember { mutableStateOf<QuestionData?>(null) }

    // مرّر القائمة الأولية إلى ViewModel عند تغيّرها
    LaunchedEffect(questions) {
        viewModel.setQuestions(questions)
    }

    // القائمة الحيّة من الـ ViewModel (تتحدّث تلقائيًا بعد الحذف)
    val uiQuestions by viewModel.questions.collectAsState()

    // ديالوج تأكيد الحذف
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من أنك تريد حذف هذا السؤال؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        questionToDelete?.let { viewModel.deleteQuestion(it.id) }
                        showDeleteConfirmationDialog = false
                        questionToDelete = null
                    }) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("إلغاء")
                }
            })
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "عنوان الاختبار", onNavigateUp = onNavigateUp
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiQuestions) { question -> // ← استبدال القائمة الوهمية
                    QuestionSummaryItem(
                        questionText = question.text,
                        onEditClick = {
                            onEditQuestion(question) // <--- 2. استخدم الدالة الجديدة ومرّر السؤال
                        }, onDeleteClick = {
                            questionToDelete = question
                            showDeleteConfirmationDialog = true
                        })
                }
            }

            val context = LocalContext.current
            AppButton(
                text = "نشر الاختبار",
                onClick = {
                    Toast.makeText(context, "تم نشر الاختبار بنجاح ✅", Toast.LENGTH_SHORT).show()

                    val exam = Exam(
                        className = examState.selectedClass,
                        examTitle = examState.examTitle,
                        examType = examState.examType,
                        examDate = examState.examDate,
                        examStartTime = examState.examStartTime,
                        examTime = examState.examTime,
                        randomQuestions = examState.randomQuestions,
                        showResultsImmediately = examState.showResultsImmediately,
                        teacherId = examState.teacherId,
                        teacherName = examState.teacherName,
                        createdAt = examState.createdAt,
                        questions = uiQuestions
                    )

                    // في onClick الزر
                    var isPublishing = true
                    viewModel.publishExam(
                        examState,
                        onSuccess = {
                            isPublishing = false
                            Toast.makeText(context, "تم نشر الاختبار بنجاح", Toast.LENGTH_SHORT)
                                .show()
                            onPublish()
                        },
                        onError = { msg ->
                            isPublishing = false
                            Toast.makeText(context, "فشل النشر: $msg", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                enabled = uiQuestions.isNotEmpty()
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