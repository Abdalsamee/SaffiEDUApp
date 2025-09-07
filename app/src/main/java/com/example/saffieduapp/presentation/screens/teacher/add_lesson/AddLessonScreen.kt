package com.example.saffieduapp.presentation.screens.teacher.add_lesson

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.components.PrimaryButton
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.ClassDropdown
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.FilePickerBox
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.LessonDatePicker
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.NotificationSwitch
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLessonScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddLessonViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // --- ١. تعريف مشغّل منتقي الفيديو ---
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.onEvent(AddLessonEvent.VideoSelected(uri))
        }
    )

    // --- ٢. تعريف مشغّل منتقي الملفات (للـ PDF) ---
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            viewModel.onEvent(AddLessonEvent.PdfSelected(uri))
        }
    )

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة دروس",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                AddLessonTextField(
                    title = "أضف عنوان الدرس",
                    value = state.lessonTitle,
                    onValueChange = { viewModel.onEvent(AddLessonEvent.TitleChanged(it)) },
                    placeholder = "أدخل عنوان الدرس",
                    singleLine = true
                )

                AddLessonTextField(
                    title = "الوصف",
                    value = state.description,
                    onValueChange = { viewModel.onEvent(AddLessonEvent.DescriptionChanged(it)) },
                    placeholder = "أدخل الوصف / الأهداف",
                    modifier = Modifier.height(100.dp),
                    enabled = state.selectedContentType != ContentType.PDF
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "إضافة الصف",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    ClassDropdown(
                        selectedClass = state.selectedClass,
                        onClassSelected = { selected ->
                            viewModel.onEvent(AddLessonEvent.ClassSelected(selected))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilePickerBox(
                        modifier = Modifier.weight(1f),
                        label = "إضافة ملف PDF",
                        enabled = state.selectedContentType != ContentType.VIDEO,
                        selectedFileName = state.selectedPdfName,
                        onClearSelection = {
                            viewModel.onEvent(AddLessonEvent.ClearPdfSelection)
                        },
                        onClick = {
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf, contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (state.selectedContentType != ContentType.VIDEO) AppPrimary else Color.Gray
                        )
                    }

                    FilePickerBox(
                        modifier = Modifier.weight(1f),
                        label = "إضافة فيديو",
                        enabled = state.selectedContentType != ContentType.PDF,
                        selectedFileName = state.selectedVideoName,
                        onClearSelection = {
                            viewModel.onEvent(AddLessonEvent.ClearVideoSelection)
                        },
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (state.selectedContentType != ContentType.PDF) AppPrimary else Color.Gray
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "إضافة تاريخ النشر",
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    LessonDatePicker(
                        selectedDate = state.publicationDate,
                        onDateSelected = { viewModel.onEvent(AddLessonEvent.DateChanged(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                NotificationSwitch(
                    text = "إشعار للطلاب",
                    isChecked = state.notifyStudents,
                    onCheckedChange = { isEnabled ->
                        viewModel.onEvent(AddLessonEvent.NotifyStudentsToggled(isEnabled))
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                AppButton(
                    text = "حفظ ونشر للطلاب",
                    onClick = { viewModel.onEvent(AddLessonEvent.SaveClicked) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
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

//@Preview(showBackground = true, locale = "ar", showSystemUi = true)
//@Composable
//private fun AddLessonScreenPreview() {
//    SaffiEDUAppTheme {
//        AddLessonScreen(onNavigateUp = {})
//    }
//}