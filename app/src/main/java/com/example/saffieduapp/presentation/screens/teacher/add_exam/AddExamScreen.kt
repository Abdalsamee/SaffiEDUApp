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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_alert.components.TimePickerField
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.ExamTypeDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.TimeDurationPicker
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.LessonDatePicker
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.NotificationSwitch
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.components.ClassDropdown
import com.example.saffieduapp.ui.theme.AppPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamScreen(
    onNavigateUp: () -> Unit,
    onNavigateToNext: (AddExamState) -> Unit,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp), // المسافة بين العناصر
                    verticalAlignment = Alignment.CenterVertically      // محاذاة العناصر عموديًا
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Text(
                            text = "وقت بدء الاختبار",
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = Color.Black,

                            )

                        TimePickerField(
                            selectedTime = state.examStartTime,
                            onTimeSelected = { hour, minute ->
                                // 👇 هنا نضمن أن الوقت يُخزَّن بصيغة موحدة 24-ساعة
                                val formattedTime = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute)
                                viewModel.onEvent(AddExamEvent.StartTimeChanged(formattedTime))
                            },
                            modifier = Modifier.fillMaxWidth(0.4f)
                        )
                    }


                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "اضافة تاريخ الاختبار",
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                        LessonDatePicker(
                            selectedDate = state.examDate,
                            onDateSelected = { millis ->
                                val formatted =
                                    SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                                        Date(millis)
                                    )
                                viewModel.onEvent(AddExamEvent.DateChanged(formatted))
                            }
                        )
                    }
                }

                Column() {
                    Text(
                        text = "إضافة مدة زمنية",
                        fontWeight = FontWeight.Normal, fontSize = 18.sp
                    )
                    TimeDurationPicker(
                        value = state.examTime,
                        onValueChange = {
                            viewModel.onEvent(AddExamEvent.TimeChanged(it))
                        }
                    )
                }
                NotificationSwitch(
                    text = "ترتيب الأسئلة عشوائيًا",
                    isChecked = state.randomQuestions,
                    onCheckedChange = { isEnabled ->
                        viewModel.onEvent(AddExamEvent.RandomQuestionsToggled(isEnabled))
                    }
                )
                NotificationSwitch(
                    text = "عرض النتائج مباشرة بعد الانتهاء",
                    isChecked = state.showResultsImmediately,
                    onCheckedChange = { isEnabled ->
                        viewModel.onEvent(AddExamEvent.ShowResultsToggled(isEnabled))
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                AppButton(
                    text = "التالي",
                    onClick = {
                        viewModel.fetchTeacherInfo { id, name ->
                            val updatedState = state.copy(
                                teacherId = id,
                                teacherName = name
                            )
                            onNavigateToNext(updatedState)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.examTitle.isNotBlank() &&
                            state.selectedClass.isNotBlank()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // زر "حفظ كمسودة"
            Button(
                onClick = { viewModel.onEvent(AddExamEvent.SaveDraftClicked) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isDraftSaved) Color.Gray else AppPrimary
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 20.dp),
                enabled = !state.isDraftSaved
            ) {
                Text(
                    text = if (state.isDraftSaved) "تم الحفظ" else "حفظ كمسودة",
                    color = Color.White
                )
            }

        }
    }
}
//@Preview(showBackground = true, locale = "ar")
//@Composable
//private fun AddExamScreenPreview() {
//    SaffiEDUAppTheme {
//        AddExamScreen(
//            onNavigateUp = {}
//        )
//    }
//}