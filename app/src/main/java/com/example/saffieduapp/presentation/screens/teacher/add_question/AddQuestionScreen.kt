package com.example.saffieduapp.presentation.screens.teacher.add_question

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.LessonDatePicker
import com.example.saffieduapp.presentation.screens.teacher.add_question.components.PointsDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_question.components.QuestionTypeDropdown
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
@Composable
fun AddQuestionScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddQuestionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    AddQuestionScreenContent(
        state = state,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuestionScreenContent(
    state: AddQuestionState,
    onNavigateUp: () -> Unit,
    onEvent: (AddQuestionEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة أسئلة",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
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
                    selectedType = state.currentQuestionType,
                    onTypeSelected = { newType ->
                        onEvent(AddQuestionEvent.QuestionTypeChanged(newType))
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "اضافة السؤال",
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                PointsDropdown(
                    selectedPoints = state.currentQuestionPoints,
                    onPointsSelected = { newPoints ->
                        onEvent(AddQuestionEvent.PointsChanged(newPoints))
                    }
                )


            }
            AddLessonTextField(
                title =null ,
                value = state.currentQuestionText,
                onValueChange = { onEvent(AddQuestionEvent.QuestionTextChanged(it)) },
                placeholder = "ادخل نص السؤال",
                modifier = Modifier.height(150.dp)
            )
            /// another

        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun AddQuestionScreenPreview() {
    SaffiEDUAppTheme {
        AddQuestionScreenContent(
            state = AddQuestionState(),
            onNavigateUp = {},
            onEvent = {}
        )
    }
}