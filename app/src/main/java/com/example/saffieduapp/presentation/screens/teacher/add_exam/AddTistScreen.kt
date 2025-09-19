package com.example.saffieduapp.presentation.screens.teacher.add_exam


import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.ClassDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.NotificationSwitch
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.TistDatePicker
import com.example.saffieduapp.presentation.screens.teacher.add_exam.components.TistTime
import com.example.saffieduapp.ui.theme.AppPrimary

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    onNavigateUp: () -> Unit = {},
    viewModel: AddTestViewModel = viewModel()
) {
    val testData by viewModel.testData
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة الاختبارات",
                onNavigateUp = onNavigateUp
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentPadding = PaddingValues(16.dp)
            ) {
                AppButton(
                    text = "التالي",
                    onClick = { viewModel.goToNextStep() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // حقل "اضافة الصف"
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اضافة الصف",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ClassDropdown(
                    selectedClass = testData.className,
                    onClassSelected = { viewModel.onClassNameChanged(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // حقل "اضافة عنوان للاختبار"
            AddLessonTextField(
                title = "اضافة عنوان للاختبار",
                placeholder = "ادخل عنوان الاختبار",
                value = testData.title,
                onValueChange = { viewModel.onTitleChanged(it) }
            )

            // حقل "اضافة نوع الاختبار" (Dropdown)
            TestTypeDropdownModified(
                selectedType = testData.type,
                onTypeSelected = { viewModel.onTestTypeChanged(it) }
            )

            // الصف الذي يحتوي على تاريخ الاختبار ومدة الاختبار
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top // هذا هو المفتاح للمحاذاة الرأسية
            ) {
                // العنصر الأول (اليسار): تاريخ الاختبار (كما في الصورة)
                TistDatePicker(
                    selectedDate = testData.dateString,
                    onDateSelected = { viewModel.onDateChanged(it) },
                    modifier = Modifier.weight(1f)
                )

                // العنصر الثاني (اليمين): مدة الاختبار (كما في الصورة)
                TistTime(
                    selectedDurationMinutes = testData.durationMinutes,
                    onDurationSelected = { viewModel.onDurationChanged(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            // التبديل "ترتيب الأسئلة عشوائيا"
            NotificationSwitch(
                text = "ترتيب الأسئلة عشوائيا",
                isChecked = testData.shuffleQuestions,
                onCheckedChange = { viewModel.onShuffleQuestionsToggled(it) }
            )

            // التبديل "عرض النتائج مباشرة بعد الانتهاء"
            NotificationSwitch(
                text = "عرض النتائج مباشرة بعد الانتهاء",
                isChecked = testData.showResultsImmediately,
                onCheckedChange = { viewModel.onShowResultsImmediatelyToggled(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        var isSaved by remember { mutableStateOf(false) }
        Button(
            onClick = {
                isSaved = true
            },
            shape = RoundedCornerShape(25),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            modifier = Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestTypeDropdownModified(
    selectedType: TestType,
    onTypeSelected: (TestType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "اضافة نوع الاختبار",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = Color.Black
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                TestType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    locale = "ar",
    showSystemUi = true,
    widthDp =400
)
@Composable
private fun CreateExamScreenPreview() {
    CreateExamScreen(onNavigateUp = {})
}