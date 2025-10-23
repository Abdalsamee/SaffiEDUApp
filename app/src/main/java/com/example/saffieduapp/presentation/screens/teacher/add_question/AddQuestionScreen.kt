package com.example.saffieduapp.presentation.screens.teacher.add_question

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_question.components.PointsDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_question.components.QuestionChoicesSection
import com.example.saffieduapp.presentation.screens.teacher.add_question.components.QuestionTypeDropdown
import com.example.saffieduapp.ui.theme.AppPrimary


@Composable
fun AddQuestionScreen(
    navController: NavController,
    onNavigateUp: () -> Unit,
    questionToEdit: QuestionData? = null,
    viewModel: AddQuestionViewModel = hiltViewModel(),
    onNavigateToSummary: (List<QuestionData>) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // 💡 الاستماع إلى تدفق الأحداث
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddQuestionUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(
                        context, event.message, android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(questionToEdit) {
        if (questionToEdit != null) {
            viewModel.setQuestionForEditing(questionToEdit)
        }
    }

    AddQuestionScreenContent(
        navController = navController,
        state = state,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent,
        onNavigateToSummary = onNavigateToSummary,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuestionScreenContent(
    state: AddQuestionState,
    onNavigateUp: () -> Unit,
    onEvent: (AddQuestionEvent) -> Unit,
    onNavigateToSummary: (List<QuestionData>) -> Unit,
    navController: NavController,
    // **استقبال ViewModel لتنفيذ عملية الحفظ المتزامن**
    viewModel: AddQuestionViewModel // يجب إضافة هذا المعامل
) {
    Scaffold(
        topBar = {
            // التعديل 4: تغيير العنوان بناءً على حالة التعديل
            CommonTopAppBar(
                title = if (state.isEditing) "تعديل السؤال" else "إضافة أسئلة", // <---
                onNavigateUp = onNavigateUp
            )
        }) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "اضافة نوع السؤال",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    QuestionTypeDropdown(
                        selectedType = state.currentQuestionType, onTypeSelected = { newType ->
                            onEvent(AddQuestionEvent.QuestionTypeChanged(newType))
                        })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "اضافة السؤال",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                    PointsDropdown(
                        selectedPoints = state.currentQuestionPoints,
                        onPointsSelected = { newPoints ->
                            onEvent(AddQuestionEvent.PointsChanged(newPoints))
                        })
                }
                AddLessonTextField(
                    title = null,
                    value = state.currentQuestionText,
                    onValueChange = { onEvent(AddQuestionEvent.QuestionTextChanged(it)) },
                    placeholder = "ادخل نص السؤال",
                    modifier = Modifier.height(150.dp)
                )

                QuestionChoicesSection(
                    questionType = state.currentQuestionType,
                    choices = state.currentChoices,
                    essayAnswer = state.currentEssayAnswer,
                    onEvent = onEvent
                )
                Spacer(modifier = Modifier.weight(1f)) // لدفع الأزرار لأسفل
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Button(
                        onClick = { onEvent(AddQuestionEvent.AddNewQuestionClicked) },
                        modifier = Modifier.fillMaxWidth(0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "سؤال جديد",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            // **التعديل هنا:** استخدم الدالة المتزامنة للحفظ والتحديث
                            val savedQuestion = viewModel.saveCurrentQuestionAndResetSync()

                            // 🛑 إذا كانت القيمة فارغة، هذا يعني أن التحقق فشل وقد تم عرض رسالة تنبيه
                            if (savedQuestion != null) {
                                // القائمة الآن محدّثة بالكامل في الـ ViewModel
                                val updatedQuestions = viewModel.getCreatedQuestions()

                                // 2. تمرير القائمة المحدّثة والانتقال
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "questions", updatedQuestions
                                )
                                navController.navigate(Routes.QUIZ_SUMMARY_SCREEN)
                            }
                        }, modifier = Modifier.fillMaxWidth(0.7f), shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "حفظ ونشر",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

            }

            var isSaved by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    isSaved = true // عند الضغط، النص يتغير
                },
                shape = RoundedCornerShape(25),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 25.dp)
            ) {
                Text(
                    text = if (isSaved) "تم الحفظ" else "حفظ كمسودة",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}


//@Preview(showBackground = true, locale = "ar")
//@Composable
//private fun AddQuestionScreenPreview() {
//    SaffiEDUAppTheme {
//        AddQuestionScreenContent(
//            state = AddQuestionState(),
//            onNavigateUp = {},
//            onEvent = {}
//        )
//    }
//}