package com.example.saffieduapp.presentation.screens.teacher.add_exam

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.ExamTypeDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.components.ClassDropdown
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة اختبار",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // مساحة فارغة لتجنب التداخل مع زر "حفظ كمسودة"
                Spacer(modifier = Modifier.height(10.dp))

                // حقل اختيار الصف
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "إضافة الصف",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )

                    ClassDropdown(
                        selectedClass = state.selectedClass,
                        onClassSelected = {
                            viewModel.onEvent(AddExamEvent.ClassSelected(it))
                        }
                    )
                }
                AddLessonTextField(
                    title = "إضافة عنوان للاختبار",
                    value = state.examTitle,
                    onValueChange = { viewModel.onEvent(AddExamEvent.TitleChanged(it)) },
                    placeholder = "أدخل عنوان الاختبار",
                    singleLine = true
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "إختر نوع الاختبار",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )

                    ExamTypeDropdown(
                        selectedType = state.examType,
                        onTypeSelected = {
                            viewModel.onEvent(AddExamEvent.TypeChanged(it))
                        }
                    )
                }


            }

            // زر "حفظ كمسودة"
            Button(
                onClick = { /* TODO: Handle save as draft */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 20.dp)
            ) {
                Text(text = "حفظ كمسودة", color = Color.White)
            }
        }
    }
}
@Preview(showBackground = true, locale = "ar")
@Composable
private fun AddExamScreenPreview() {
    SaffiEDUAppTheme {
        AddExamScreen(
            onNavigateUp = {}
        )
    }
}