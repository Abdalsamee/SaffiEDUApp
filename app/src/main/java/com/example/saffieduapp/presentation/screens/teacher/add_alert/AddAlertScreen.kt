package com.example.saffieduapp.presentation.screens.teacher.add_alert

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_alert.components.TimePickerField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.LessonDatePicker
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.components.ClassDropdown


@Composable
fun AddAlertScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddAlertViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // هذه الدالة الآن مسؤولة فقط عن جلب الحالة وتمريرها
    AddAlertScreenContent(
        state = state,
        onNavigateUp = onNavigateUp,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSendClick = viewModel::sendAlert
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun AddAlertScreenContent(
    state: AddAlertState,
    onNavigateUp: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة تنبيه",
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
        ) {
            AddLessonTextField(
                title = "إضافة وصف للتنبيه",
                value = state.alertDescription,
                onValueChange = onDescriptionChange,
                placeholder = "أدخل وصف التنبيه ...",
                modifier = Modifier.height(150.dp),

                )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)){

                Text(
                    text = "الصف المستهدف",
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                ClassDropdown(
                    selectedClass = state.targetClass,
                    onClassSelected = { selected ->

                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // المسافة بين العناصر
                verticalAlignment = Alignment.CenterVertically      // محاذاة العناصر عموديًا
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ){

                    Text(
                        text = "وقت الارسال",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    TimePickerField(
                        selectedTime = state.sendTime,
                        onTimeSelected = {},
                        modifier = Modifier.fillMaxWidth(0.3f)
                    )
                }
                

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "تاريخ الارسال",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    LessonDatePicker(
                        selectedDate = state.sendTime,
                        onDateSelected = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppButton(
                text = "إرسال التنبيه",
                onClick = onSendClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            )
        }
    }
}

//@Preview(showBackground = true, locale = "ar")
//@Composable
//private fun AddAlertScreenPreview() {
//    SaffiEDUAppTheme {
//        val previewState = AddAlertState()
//        AddAlertScreenContent(
//            state = previewState,
//            onNavigateUp = {},
//            onDescriptionChange = {},
//            onSendClick = {}
//        )
//    }
//}